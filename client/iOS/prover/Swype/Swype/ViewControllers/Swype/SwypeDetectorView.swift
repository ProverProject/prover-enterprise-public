import UIKit

protocol SwypeDetectorCoordinateDelegate: class {
    func detectorDidChangeDirection(to: SwypeVector?)
    func detectorDidChangeCoordinates(x: CGFloat, y: CGFloat)
}

class SwypeDetectorView: UIView {
    
    // MARK: - IBOutlet
    @IBOutlet private weak var targetContainerView: TargetContainerView!
    @IBOutlet private weak var roundMovementView: UIImageView! {
        didSet {
            let images: [UIImage] = Array(1...90).compactMap {
                guard let path = Bundle.main.path(forResource: "round_move_\($0)", ofType: "png") else {
                    debugPrint("Failed image name (1) is", $0)
                    return nil
                }
                let image = UIImage(contentsOfFile: path)
                return image
            }

            roundMovementView.animationImages = images
            roundMovementView.animationDuration = 3
        }
    }

    private let targetMaxDistance: CGFloat = {
        let screenSize = UIScreen.main.bounds
        let distance = 0.5 * min(screenSize.width, screenSize.height)

        return distance
    }()

    private let directionLineMaxDistance: CGFloat = {
        let screenSize = UIScreen.main.bounds

        // To make line crossing the whole screen
        let distance = max(screenSize.width, screenSize.height)

        return distance
    }()
}

// MARK: - Public methods
extension SwypeDetectorView {

    func showRingAndTargetViews() {
        targetContainerView.setAnimatedVisibility(visible: true)
    }

    func hideRingAndTargetViews() {
        targetContainerView.setAnimatedVisibility(visible: false) { [unowned self] _ in
            self.targetContainerView.targetPosition = (x: 0, y: 0)
            self.targetContainerView.path = nil
        }
    }
}

// MARK: - DetectorCoordinateDelegate
extension SwypeDetectorView: SwypeDetectorCoordinateDelegate {
    func detectorDidChangeDirection(to vector: SwypeVector?) {
        guard let vector = vector else { return }

        DispatchQueue.main.async { [unowned self] in

            // Draw line from the initial target point to the center
            let center = CGPoint(x: self.bounds.midX, y: self.bounds.midY)
            let start = CGPoint(x: center.x + CGFloat(vector.x) * self.directionLineMaxDistance,
                                y: center.y + CGFloat(vector.y) * self.directionLineMaxDistance)

            self.targetContainerView.path = self.createLinePath(start, center)
        }
    }
    
    func detectorDidChangeCoordinates(x: CGFloat, y: CGFloat) {
        DispatchQueue.main.async { [unowned self] in

            // Update the target's coordinates
            let normalizedDistance = 0.75 * self.targetMaxDistance
            self.targetContainerView.targetPosition = (x: normalizedDistance * x,
                                                       y: normalizedDistance * y)
        }
    }
    
    private func createLinePath(_ start: CGPoint, _ end: CGPoint) -> CGPath {
        let path = UIBezierPath()
        
        path.move(to: start)
        path.addLine(to: end)
        
        return path.cgPath
    }
}

extension SwypeDetectorView {
    
    func update(by state: SwypeViewControllerState) {
        
        print("[TargetView] switch to \(state)")
        
        switch state {
        case .waitingToStartSwypeCode,
             .detectingSwypeCode:
            showRingAndTargetViews()

        case .requestingBalanceAndPriceOnAppearance,
             .readyToGetSwypeCode,
             .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .waitingForCode,
             .didReceiveCode,
             .waitingForCircle,
             .finishingWithoutDetectedSwypeCode,
             .swypeCodeDetected,
             .finishingWithDetectedSwypeCode,
             .submittingMediaHash,
             .confirmingMediaHashSubmission:
            hideRingAndTargetViews()
        }

        switch state {
        case .waitingForCircle:
            roundMovementView.start()

        case .requestingBalanceAndPriceOnAppearance,
             .readyToGetSwypeCode,
             .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .waitingForCode,
             .didReceiveCode,
             .waitingToStartSwypeCode,
             .detectingSwypeCode,
             .finishingWithoutDetectedSwypeCode,
             .swypeCodeDetected,
             .finishingWithDetectedSwypeCode,
             .submittingMediaHash,
             .confirmingMediaHashSubmission:
            roundMovementView.stop()
        }
    }
}
