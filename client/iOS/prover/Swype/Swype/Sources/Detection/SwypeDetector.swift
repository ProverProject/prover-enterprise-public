import Foundation
import UIKit
import Common

class SwypeDetector {
    // MARK: - Public methods
    func getSwypeCode() -> String? {
        return swypeCode
    }

    func setSwypeCode(_ code: String) {
        swypeCode = code
        swypeDirections = Array(code).compactMap { Int(String($0)) }

        wrapper.setSwype(code)
    }

    func getTimeStamps() -> [UInt]? {
        guard !timeStamps.isEmpty && timeStamps.count == swypeDirections.count else {
            // Detection has been interrupted
            return nil
        }
        return timeStamps
    }

    func getMinProcessingTime() -> UInt {
        return UInt(minFrameProcessingTime)
    }

    func getMaxProcessingTime() -> UInt {
        return UInt(maxFrameProcessingTime)
    }

    func getTotalTime() -> UInt {
        return UInt(currentTimeStamp)
    }

    func getTotalFrames() -> UInt {
        return totalFramesCount > 0 ? UInt(totalFramesCount) : 0
    }

    func getSwypeHelperVersion() -> Int {
        return Int(wrapper.getSwypeHelperVersion())
    }

    // MARK: - Private properties
    private var swypeCode: String?
    private var swypeDirections: [Int] = []
    private var timeStamps = [UInt]()

    // properties for detector

    private var state: Int32 = -1
    private var index: Int32 = 0
    private var message: Int32 = 0
    private var debug: Int32 = 0

    private var currentDirection: SwypeVector?

    // MARK: - Dependencies
    private let wrapper: SwypeDetectorWrapper

    public var detectorXScale: Double {
        return wrapper.detectorXScale()
    }
    public var detectorYScale: Double {
        return wrapper.detectorYScale()
    }

    private weak var stateDelegate: SwypeDetectorStateDelegate?
    private weak var coordinateDelegate: SwypeDetectorCoordinateDelegate?

    private var currentTimeStamp: UInt32 = 0
    private var totalFramesCount: Int64 = -1

    private var fpsCalcTimeStamp: CMTime = CMTime()
    private var fpsCalcFramesCount: Int64 = -1

    private var totalDetectedFramesCount: Int64 = 0

    private var minFrameProcessingTime: UInt32 = 0x7fffffff
    private var maxFrameProcessingTime: UInt32 = 0

    private let detectorQueue = DispatchQueue(label: "detectorQueue")
    private var pendingFrames: UInt32 = 0

    private let isUsingFacingCamera: Bool = Settings.cameraPosition == .front
    private let invertSwypeDirectionsForFacingCamera: Bool = Settings.invertSwypeDirectionsForFacingCamera

    // MARK: - Lifecycle
    init(frameWidth: UInt, frameHeight: UInt, stateDelegate: SwypeDetectorStateDelegate?, coordinateDelegate: SwypeDetectorCoordinateDelegate?) {
        self.wrapper = SwypeDetectorWrapper()
        self.stateDelegate = stateDelegate
        self.coordinateDelegate = coordinateDelegate

        coordinateDelegate?.detectorDidChangeDirection(to: .zero)
        coordinateDelegate?.detectorDidChangeCoordinates(x: 0, y: 0)

        print("[SwypeDetector] init private var state: Int32 = \(state)")
        //print("[SwypeDetector] init private var detectorState: State = \(SwypeDetectorState(rawValue: state)!)")
    }
    
    deinit {
        print("[SwypeDetector] deinit")
        //stateDelegate.updateVideoDetectionFps(0)
    }

    // MARK: - Process frame
    func process(_ imageBuffer: CVImageBuffer, timestamp: CMTime, callingQueue: DispatchQueue) {
        guard pendingFrames < 3 else {
            return
        }

        pendingFrames += 1

        detectorQueue.async { [weak self] in
            guard self != nil else { return }
            
            self!.doProcess(imageBuffer, timestamp: timestamp)

            callingQueue.async { [weak self] in
                if self != nil {
                    self!.pendingFrames -= 1
                }
            }
        }
    }

    private func doProcess(_ imageBuffer: CVImageBuffer, timestamp: CMTime) {

        let prevState: Int32 = state
        let prevIndex: Int32 = index
        var xValue: Int32 = 0
        var yValue: Int32 = 0
        
        wrapper.processFrame(
                imageBuffer,
                timestamp: currentTimeStamp,
                state: &state,
                index: &index,
                x: &xValue,
                y: &yValue,
                message: &message,
                debug: &debug)

        totalFramesCount += 1
        fpsCalcFramesCount += 1

        if totalFramesCount != 0 {
            if totalFramesCount != 0 && totalFramesCount % 10 == 0 {
                if fpsCalcTimeStamp.timescale != 0 {
                    let elapsedTimeStamp = timestamp - fpsCalcTimeStamp
                    let fps = Double(fpsCalcFramesCount * Int64(elapsedTimeStamp.timescale)) / Double(elapsedTimeStamp.value)

                    stateDelegate?.detectorDidUpdateFps(fps)
                    //debugPrint("[SwypeDetector] for \(fpsCalcFramesCount) frames FPS is \(fps)")
                }

                fpsCalcTimeStamp = timestamp
                fpsCalcFramesCount = 0
            }

            let newTimeStamp = UInt32(1000 * timestamp.value / Int64(timestamp.timescale))

            calcMinMaxFrameProcessingTimes(newTimeStamp)
        }

        if state != prevState {
            let prevDetectorState = SwypeDetectorState(rawValue: prevState)
            let detectorState = SwypeDetectorState(rawValue: state)!

            coordinateDelegate?.detectorDidChangeState(from: prevDetectorState, to: detectorState)

            if state == SwypeDetectorState.swypeCodeDetected.rawValue {
                debugPrint("[SwypeDetector] total: \(totalDetectedFramesCount)")
                //debugPrint("[SwypeDetector] dropped: \(droppedDetectedFramesCount)")
            }
        }

        if state == SwypeDetectorState.detectingSwypeCode.rawValue {
            if index < 1 {
                fatalError("[SwypeDetector] index is lower than 1!")
            }

            // When detector changes index, it writes to xValue and yValue target coordinates for the
            // _old_ direction, so we update target coordinates only when index preserves its value
            if state != prevState || index != prevIndex {
                let directionRawValue = swypeDirections[Int(index) - 1]
                
                appendTimeStamp(timestamp: timestamp, clearTimeStamps: index == 1)

                currentDirection = SwypeVector(rawValue: directionRawValue)

                coordinateDelegate?.detectorDidChangeSwypeIndex(to: Int(index))
                coordinateDelegate?.detectorDidChangeDirection(to: currentDirection)
            }
            else {
                guard let direction = currentDirection else {
                    return
                }

                var x = CGFloat(direction.x) - CGFloat(xValue) / CGFloat(1024)
                var y = CGFloat(direction.y) - CGFloat(yValue) / CGFloat(1024)

                if isUsingFacingCamera {
                    if invertSwypeDirectionsForFacingCamera {
                        y = -y
                    }
                    else {
                        x = -x
                    }
                }

                coordinateDelegate?.detectorDidChangeCoordinates(x: x, y: y)
            }
        }
    }

    private func appendTimeStamp(timestamp: CMTime, clearTimeStamps: Bool) {
        if clearTimeStamps {
            timeStamps.removeAll()
        }

        let uintTimeStamp = UInt32(1000 * timestamp.value / Int64(timestamp.timescale))

        if clearTimeStamps {
            debugPrint("[SwypeDetector] First timestamp will be", uintTimeStamp)
        }
        
        timeStamps.append(UInt(uintTimeStamp))
    }

    private func calcMinMaxFrameProcessingTimes(_ newTimeStamp: UInt32) {
        let frameProcessingTime = newTimeStamp - currentTimeStamp

        if minFrameProcessingTime > frameProcessingTime {
            minFrameProcessingTime = frameProcessingTime
        }

        if maxFrameProcessingTime < frameProcessingTime {
            maxFrameProcessingTime = frameProcessingTime
        }

        currentTimeStamp = newTimeStamp
    }
}
