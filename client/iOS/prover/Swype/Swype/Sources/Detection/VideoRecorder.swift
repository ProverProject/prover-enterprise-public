import UIKit
import Accelerate
import AVFoundation
import Common

class VideoRecorder: NSObject {

    // MARK: - Public properties
    public var isRunning: Bool { return captureSession.isRunning }
    public var realVideoOutputResolution: (width: UInt, height: UInt) {
        return Settings.videoOutputResolution(forPreset: realVideoOutputPreset)
    }

    // MARK: - Private properties
    private weak var videoPreviewView: VideoPreviewView!
    private weak var delegate: VideoRecorderDelegate?

    private var captureVideoPreviewLayer: AVCaptureVideoPreviewLayer!

    private var captureSession: AVCaptureSession!

    private let dataOutputQueue = DispatchQueue(label: "dataOutputQueue")

    private var isRecording = false
    private var isAssetWriterSessionStarted = false
    private var realVideoOutputPreset: AVOutputSettingsPreset!

    private var assetVideoWriterInput: AVAssetWriterInput!
    private var assetAudioWriterInput: AVAssetWriterInput!

    private var assetWriterInputPixelBufferAdaptor: AVAssetWriterInputPixelBufferAdaptor!
    private var assetWriter: AVAssetWriter!

    private var capturedFramesCount: Int64 = -1

    private var fpsCalcTimeStamp: CMTime = CMTime()
    private var fpsCalcFramesCount: Int64 = -1

    private var recordStartTimeStamp: CMTime!
    private var recordedFramesCount: Int64 = 0

    // MARK: - Dependencies
    private lazy var dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "YYYY-MM-dd__HH-mm-ss"
        return formatter
    }()
    
    private let sourceVideoURL =
            FileManager.documentURL.appendingPathComponent("recorded_video_source.mov")

    private var videoOutputSettings: [String : Any] = [:]

    // MARK: - Initialization
    init(withParent parent: VideoPreviewView, delegate: VideoRecorderDelegate?) {
        super.init()

        self.videoPreviewView = parent
        self.delegate = delegate
    }

    deinit {
        print("[VideoRecorder] deinit")
    }
}

// MARK: - Public methods
extension VideoRecorder {

    func startSession() {

        print("[VideoRecorder] Start session")

        captureVideoPreviewLayer = (videoPreviewView.layer as! AVCaptureVideoPreviewLayer)
        captureSession = AVCaptureSession()

        captureVideoPreviewLayer.session = captureSession

        captureSession.beginConfiguration()

        addCaptureVideoDeviceInput()
        addCaptureAudioDeviceInput()

        addCaptureVideoDataOutput()
        addCaptureAudioDataOutput()

        captureSession.commitConfiguration()

        let (outputPreset, capturePreset) = findTheMostAppropriateVideoPresets()

        realVideoOutputPreset = outputPreset
        captureSession.sessionPreset = capturePreset

        captureSession.startRunning()
    }

    func stopSession() {
        print("[VideoRecorder] Stop session")

        captureSession.stopRunning()

        realVideoOutputPreset = nil
        capturedFramesCount = -1
        fpsCalcFramesCount = -1
    }

    func switchCameraPosition() {
        switch Settings.cameraPosition {
        case .back:
            Settings.cameraPosition = .front
        case .front:
            Settings.cameraPosition = .back
        case .unspecified:
            fatalError("[VideoRecorder] Camera positions was not specified!")
        }
    }

    func findTheMostAppropriateVideoPresets() -> (AVOutputSettingsPreset, AVCaptureSession.Preset) {
        return Settings.availableVideoOutputPresetsUpToSelected().compactMap { outputPreset in
            let capturePreset = Settings.videoCapturePreset(for: outputPreset)

            guard captureSession.canSetSessionPreset(capturePreset) else {
                return nil
            }

            return (outputPreset, capturePreset)
        }.last!
    }
}

// MARK: - Private methods
private extension VideoRecorder {

    func addCaptureVideoDeviceInput() {

        let discoverySession = AVCaptureDevice
            .DiscoverySession(deviceTypes: [.builtInWideAngleCamera],
                              mediaType: .video,
                              position: Settings.cameraPosition)
        
        guard let captureVideoDevice = discoverySession.devices.first else {
            print("[VideoRecorder] Could not add the video device input to capture session!")
            return
        }

        // We MUST have available camera here so aren't catching any exceptions
        let captureVideoDeviceInput = try! AVCaptureDeviceInput(device: captureVideoDevice)

        try! captureVideoDevice.lockForConfiguration()

        // support for autofocus
        if captureVideoDevice.isFocusModeSupported(.continuousAutoFocus) {
            captureVideoDevice.focusMode = .continuousAutoFocus
        }
        else if captureVideoDevice.isFocusModeSupported(.autoFocus) {
            captureVideoDevice.focusMode = .autoFocus
            //fatalError("[VideoRecorder] Autofocus must be supported!")
        }

        captureVideoDevice.unlockForConfiguration()

        if (captureSession.canAddInput(captureVideoDeviceInput)) {
            captureSession.addInput(captureVideoDeviceInput)
        }
        else {
            fatalError("[VideoRecorder] Could not add the video device input to capture session!")
        }
    }

    func addCaptureAudioDeviceInput() {
        let discoverySession = AVCaptureDevice
                .DiscoverySession(deviceTypes: [.builtInMicrophone],
                mediaType: .audio,
                position: .unspecified)

        guard let captureAudioDevice = discoverySession.devices.first else {
            return
        }

        do {
            let captureAudioDeviceInput = try AVCaptureDeviceInput(device: captureAudioDevice)

            if (captureSession.canAddInput(captureAudioDeviceInput)) {
                captureSession.addInput(captureAudioDeviceInput)
            }
            else {
                print("[VideoRecorder] Could not add the audio device input to capture session!")
            }
        } catch {
            print("[VideoRecorder] Okay, mike is disabled, the video will just be silent: \(error.localizedDescription)")
        }
    }

    func addCaptureVideoDataOutput() {

        // Make a video data output
        let captureVideoDataOutput = AVCaptureVideoDataOutput()

        // In color mode we, BGRA format is used
        let format = Int(kCVPixelFormatType_32BGRA)
        captureVideoDataOutput.videoSettings = [kCVPixelBufferPixelFormatTypeKey: format] as [String: Any]

        // discard if the data output queue is blocked (as we process the still image)
        captureVideoDataOutput.alwaysDiscardsLateVideoFrames = true

        if captureSession.canAddOutput(captureVideoDataOutput) {
            captureSession.addOutput(captureVideoDataOutput)
        }

        let connection = captureVideoDataOutput.connection(with: .video)!

        connection.isEnabled = true
        connection.videoOrientation = captureVideoPreviewLayer.connection!.videoOrientation

        captureVideoDataOutput.setSampleBufferDelegate(self, queue: dataOutputQueue)
    }

    func addCaptureAudioDataOutput() {
        let captureAudioDataOutput = AVCaptureAudioDataOutput()

        if captureSession.canAddOutput(captureAudioDataOutput) {
            captureSession.addOutput(captureAudioDataOutput)
        }

        captureAudioDataOutput.connection(with: .audio)?.isEnabled = true
        captureAudioDataOutput.setSampleBufferDelegate(self, queue: dataOutputQueue)
    }
}

// MARK: - Recording
extension VideoRecorder {

    private func assetVideoSettings(_ assistant: AVOutputSettingsAssistant) -> [String : Any] {
        let videoSettings = assistant.videoSettings!
        let videoCodec = videoSettings[AVVideoCodecKey]!
        let compressionProperties = [AVVideoAllowFrameReorderingKey: false]
        let width = videoSettings[AVVideoWidthKey] as! UInt
        let height = videoSettings[AVVideoHeightKey] as! UInt

        let videoOrientation = captureVideoPreviewLayer.connection!.videoOrientation

        if videoOrientation == .landscapeLeft || videoOrientation == .landscapeRight {
            return [AVVideoCodecKey: videoCodec,
                    AVVideoCompressionPropertiesKey: compressionProperties,
                    AVVideoWidthKey: width,
                    AVVideoHeightKey: height
            ]
        }
        else {
            return [AVVideoCodecKey: videoCodec,
                    AVVideoCompressionPropertiesKey: compressionProperties,
                    AVVideoWidthKey: height,
                    AVVideoHeightKey: width]
        }
    }

    private func assetAudioSettings(_ assistant: AVOutputSettingsAssistant) -> [String : Any] {
        let audioSettings = assistant.audioSettings!
        //let format = audioSettings[AVFormatIDKey]!
        //let rate = audioSettings[AVSampleRateKey]!
        //let channels = audioSettings[AVNumberOfChannelsKey]!
        let format = kAudioFormatMPEG4AAC
        let rate = 12000
        let channels = 1

        return [AVFormatIDKey: format,
                AVSampleRateKey: rate,
                AVNumberOfChannelsKey: channels]
    }

    func startRecord() {
        UIApplication.shared.isIdleTimerDisabled = true

        let possibleVideoExtensions = [".mov", ".mp4", ".m4v"]
        
        FileManager.clearDocumentsDirectory(exts: possibleVideoExtensions)
        FileManager.clearTempDirectory(exts: possibleVideoExtensions)

        let assistant = AVOutputSettingsAssistant(preset: realVideoOutputPreset)!

        videoOutputSettings = assetVideoSettings(assistant)

        assetWriter = try! AVAssetWriter(url: sourceVideoURL, fileType: .mov)

        assetVideoWriterInput = AVAssetWriterInput(mediaType: .video, outputSettings: videoOutputSettings)
        assetVideoWriterInput.expectsMediaDataInRealTime = true

        let videoOutputAttributes = [kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA]

        assetWriterInputPixelBufferAdaptor = AVAssetWriterInputPixelBufferAdaptor(
                                   assetWriterInput: assetVideoWriterInput,
                                   sourcePixelBufferAttributes: videoOutputAttributes)

        if assetWriter.canAdd(assetVideoWriterInput) {
            assetWriter.add(assetVideoWriterInput)
        }

        let audioSettings = assetAudioSettings(assistant)

        assetAudioWriterInput = AVAssetWriterInput(mediaType: .audio, outputSettings: audioSettings)
        assetAudioWriterInput.expectsMediaDataInRealTime = true

        if assetWriter.canAdd(assetAudioWriterInput) {
            assetWriter.add(assetAudioWriterInput)
        }

        guard assetWriter.startWriting() else {
            fatalError("[VideoRecorder] Recording Error: asset writer could not start writing: \(assetWriter.error!.localizedDescription)")
        }

        isRecording = true
    }

    func stopRecord() {

        isRecording = false
        isAssetWriterSessionStarted = false

        UIApplication.shared.isIdleTimerDisabled = false

        assetWriter.finishWriting { [weak self] in
            guard let `self` = self else {
                return
            }
            
            print("[VideoRecorder] stopRecord's completion, status is \(self.assetWriter.status.rawValue)")

            if let error = self.assetWriter.error {
                print("[VideoRecorder] assetWriter.error = '\(error.localizedDescription)'")
            }

            self.disposeAssetWriter()
            self.delegate?.recordFinished(at: self.sourceVideoURL)
            self.exportMetadata()
        }
    }

    private func exportMetadata() {
        let asset = AVURLAsset(url: sourceVideoURL)
        let assetExportSession = AVAssetExportSession(asset: asset, presetName: AVAssetExportPresetPassthrough)!

        let swypeCode = delegate?.getSwypeCode()

        assetExportSession.outputURL = destVideoURL(swypeCode: swypeCode)
        assetExportSession.outputFileType = .mov

        let title = AVMutableMetadataItem()
        title.keySpace = .quickTimeMetadata
        title.key = AVMetadataKey.commonKeyTitle as NSString
        title.value = "Prover video" as NSString

        let detectorXScale = delegate?.detectorXScale ?? 1
        let detectorYScale = delegate?.detectorYScale ?? 1

        let videoOutputWidth = videoOutputSettings[AVVideoWidthKey] as! UInt
        let videoOutputHeight = videoOutputSettings[AVVideoHeightKey] as! UInt

        let detectorScaledWidth = Int(Double(videoOutputWidth) * detectorXScale)
        let detectorScaledHeight = Int(Double(videoOutputHeight) * detectorYScale)

        var proverValue = "size:\(detectorScaledWidth),\(detectorScaledHeight);"
        proverValue += "code:\(swypeCode ?? "");"

        if let swypeTimeStamps = delegate?.getTimeStamps() {
            proverValue += "\(swypeTimeStamps.map(String.init).joined(separator: ","));"
        }

        if let swypeHelperVersion = delegate?.getSwypeHelperVersion() {
            proverValue += "version:\(swypeHelperVersion);"
        }

        let prover = AVMutableMetadataItem()
        prover.keySpace = .quickTimeMetadata
        prover.key = "prover" as NSString
        prover.value = proverValue as NSString

        let device = UIDevice.current
        let minFrameTime = delegate?.getMinProcessingTime() ?? 0
        let maxFrameTime = delegate?.getMaxProcessingTime() ?? 0
        let avgFrameTime = String(format: "%.2f", Double(delegate?.getTotalTime() ?? 0) / Double(delegate?.getTotalFrames() ?? 1))
        let proverStatsValue = "device:\(device.systemName) \(device.systemVersion);" +
                               "detectionTime:\(minFrameTime),\(maxFrameTime),\(avgFrameTime);"

        let proverStats = AVMutableMetadataItem()
        proverStats.keySpace = .quickTimeMetadata
        proverStats.key = "proverStats" as NSString
        proverStats.value = proverStatsValue as NSString

        assetExportSession.metadata = [title, prover, proverStats]

        let backgroundTaskID = UIApplication.shared.beginBackgroundTask()

        assetExportSession.exportAsynchronously { [weak self] in
            guard let `self` = self else {
                return
            }
            
            let destUrl = self.destVideoURL(swypeCode: swypeCode)

            try? FileManager.default.removeItem(at: self.sourceVideoURL)
            self.delegate?.metadataSaved(at: destUrl, status: assetExportSession.status)

            UIApplication.shared.endBackgroundTask(backgroundTaskID)
        }
    }

    private func destVideoURL(swypeCode: String?) -> URL {
        let videoOutputWidth = videoOutputSettings[AVVideoWidthKey] as! UInt
        let videoOutputHeight = videoOutputSettings[AVVideoHeightKey] as! UInt
        let destMovieName: String

        if let swypeCode = swypeCode {
            destMovieName = "movie_\(videoOutputWidth)x\(videoOutputHeight)_\(swypeCode).mov"
        }
        else {
            destMovieName = "movie_\(videoOutputWidth)x\(videoOutputHeight).mov"
        }

        return FileManager.documentURL.appendingPathComponent(destMovieName, isDirectory: false)
    }

    private func disposeAssetWriter() {
        recordStartTimeStamp = nil
        assetWriterInputPixelBufferAdaptor = nil
        assetVideoWriterInput = nil
        assetAudioWriterInput = nil
        assetWriter = nil
    }
}

// MARK: - AVCaptureVideoDataOutputSampleBufferDelegate
extension VideoRecorder: AVCaptureVideoDataOutputSampleBufferDelegate, AVCaptureAudioDataOutputSampleBufferDelegate  {

    func captureOutput(_ output: AVCaptureOutput,
                       didOutput sampleBuffer: CMSampleBuffer,
                       from connection: AVCaptureConnection)
    {
        guard CMSampleBufferDataIsReady(sampleBuffer) else {
            return
        }

        let sourceSampleTimeStamp = CMSampleBufferGetPresentationTimeStamp(sampleBuffer)

        if output is AVCaptureVideoDataOutput {
            capturedFramesCount += 1
            fpsCalcFramesCount += 1

            if capturedFramesCount != 0 && capturedFramesCount % 15 == 0 {
                if fpsCalcTimeStamp.timescale != 0 {
                    let elapsedCaptureTimeStamp = sourceSampleTimeStamp - fpsCalcTimeStamp
                    let fps = Double(fpsCalcFramesCount * Int64(elapsedCaptureTimeStamp.timescale)) / Double(elapsedCaptureTimeStamp.value)

                    delegate?.recorderDidUpdateFps(fps)
                    //print("[VideoRecorder] for \(fpsCalcFramesCount) frames FPS is \(fps)")
                }

                fpsCalcTimeStamp = sourceSampleTimeStamp
                fpsCalcFramesCount = 0
            }
        }

        guard isRecording else {
            return
        }

        if recordStartTimeStamp == nil {
            recordStartTimeStamp = sourceSampleTimeStamp

            // Add a little delay since capture session seems to dim video frames over
            // first ~0.5 sec immediately after being started
            recordStartTimeStamp.value += Int64(recordStartTimeStamp.timescale) * 50 / 100
        }

        guard sourceSampleTimeStamp >= recordStartTimeStamp else {
            return
        }

        if !isAssetWriterSessionStarted {
            recordedFramesCount = 0
            recordStartTimeStamp = sourceSampleTimeStamp
            assetWriter.startSession(atSourceTime: sourceSampleTimeStamp)

            isAssetWriterSessionStarted = true
        }

        //guard assetWriter.status == .writing else {
        //    isRecording = false
        //    isAssetWriterSessionStarted = false
        //
        //    delegate.recordFailed()
        //
        //    return
        //}

        switch output {
        case is AVCaptureVideoDataOutput:
            guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
            let elapsedRecordingTimeStamp = sourceSampleTimeStamp - recordStartTimeStamp

            /*
            recordedFramesCount += 1

            if recordedFramesCount % 15 == 0 {
                let fps = Double(recordedFramesCount * Int64(elapsedRecordingTimeStamp.timescale)) / Double(elapsedRecordingTimeStamp.value)
                delegate.updateVideoCaptureFps(fps)
                print("[VideoRecorder] for \(recordedFramesCount) frames FPS is \(fps)")
            }
             */

            recordVideoImageBuffer(imageBuffer, connection, sourceSampleTimeStamp)
            delegate?.process(buffer: imageBuffer, timestamp: elapsedRecordingTimeStamp, callingQueue: dataOutputQueue)

        case is AVCaptureAudioDataOutput:
            recordAudioSampleBuffer(sampleBuffer, connection)

        default:
            print("[VideoRecorder] output is neither AVCaptureVideoDataOutput nor AVCaptureAudioDataOutput!")
        }
    }

    fileprivate func recordVideoImageBuffer(_ imageBuffer: CVImageBuffer, _ connection: AVCaptureConnection,
                                            _ sourceSampleTimeStamp: CMTime) {
        if assetVideoWriterInput.isReadyForMoreMediaData {
            guard assetWriterInputPixelBufferAdaptor.append(imageBuffer, withPresentationTime: sourceSampleTimeStamp) else {
                print("[VideoRecorder] Video frame writing Error")
                return
            }
        }
    }

    fileprivate func recordAudioSampleBuffer(_ sampleBuffer: CMSampleBuffer, _ connection: AVCaptureConnection) {
        if assetAudioWriterInput.isReadyForMoreMediaData {
            guard assetAudioWriterInput.append(sampleBuffer) else {
                print("[VideoRecorder] Audio frame writing Error")
                return
            }
        }
    }
}
