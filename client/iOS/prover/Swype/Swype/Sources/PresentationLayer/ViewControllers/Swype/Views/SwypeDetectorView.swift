import UIKit
import Common

protocol SwypeDetectorCoordinateDelegate: class {
    func detectorDidChangeState(from: SwypeDetectorState?, to: SwypeDetectorState)
    func detectorDidChangeSwypeIndex(to index: Int)
    func detectorDidChangeDirection(to: SwypeVector?)
    func detectorDidChangeCoordinates(x: CGFloat, y: CGFloat)
}

class SwypeDetectorView: UIView {
    
    // MARK: - IBOutlet
    @IBOutlet private weak var progressSw: DotsStackView!
    @IBOutlet private weak var progressSwype: UIPageControl!
    @IBOutlet private weak var titleLabel: UILabel!
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
    
    @IBOutlet private weak var swypeEnteredImageView: UIImageView!
    @IBOutlet private weak var swypeDirectionContainer: SwypeDirectionContainerView!
    
    @IBOutlet private weak var swypeEnteredViewWidthConstraint: NSLayoutConstraint!
    @IBOutlet private weak var swypeEnteredViewHeightConstraint: NSLayoutConstraint!
    
    @IBOutlet private var swypeEnteredViewCenterXConstraint: NSLayoutConstraint!
    @IBOutlet private var swypeEnteredViewCenterYConstraint: NSLayoutConstraint!
    
    @IBOutlet private var swypeEnteredViewCornerXConstraint: NSLayoutConstraint!
    @IBOutlet private var swypeEnteredViewCornerYConstraint: NSLayoutConstraint!

    private let swypeEnteredViewInitWidth: CGFloat = 187
    private let swypeEnteredViewInitHeight: CGFloat = 85

    private let targetMaxDistance: CGFloat = {
        let screenSize = UIScreen.main.bounds
        let distance = 0.5 * min(screenSize.width, screenSize.height)

        return distance
    }()

    var isSwypeCodeDetected: Bool {
        return currentDetectorState == .swypeCodeDetected
    }
    
    private var currentDetectorState: SwypeDetectorState?
    private var isCompletingAnimation: Bool = false
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        self.progressSw.normalColor = .white
        self.progressSw.scale = 1.3
    }
    
    func setSwypeLength(to swypeLength: Int) {
        DispatchQueue.main.async { [weak self] in
            self?.progressSw.createDots(swypeLength)
            //self?.progressSwype.setNumberOfPages(number: swypeLength)
        }
    }
}

// MARK: - DetectorCoordinateDelegate
extension SwypeDetectorView: SwypeDetectorCoordinateDelegate {
    
    func detectorDidChangeSwypeIndex(to index: Int) {
        DispatchQueue.main.async { [weak self] in
            self?.progressSw.updateControls(index - 1)
            //self?.progressSwype.setCurrentStep(index - 2)
        }
    }

    func detectorDidChangeDirection(to vector: SwypeVector?) {

        DispatchQueue.main.async { [weak self] in
            self?.isCompletingAnimation = true
            self?.swypeDirectionContainer.resetAnimatedTargetPosition(animated: true) { [weak self] _ in
                self?.isCompletingAnimation = false
            }
        }
    }
    
    func detectorDidChangeCoordinates(x: CGFloat, y: CGFloat) {

        DispatchQueue.main.async { [weak self] in
            guard let `self` = self else {
                return
            }
            
            guard !self.isCompletingAnimation else {
                return
            }

            // Update the target's coordinates
            let normalizedDistance = 0.75 * self.targetMaxDistance
            let xPos = normalizedDistance * x
            let yPos = normalizedDistance * y

            self.swypeDirectionContainer.targetPosition = (x: xPos, y: yPos)
        }
    }
    
    func detectorDidChangeState(from prevState: SwypeDetectorState?, to newState: SwypeDetectorState) {

        DispatchQueue.main.async { [weak self] in
            
            guard let `self` = self else {
                return
            }

            switch newState {
            case .waitingForCode:
                break

            case .waitingForCircle:
                let rollingBackToCircle: Bool = (prevState != nil && newState < prevState!)

                self.isCompletingAnimation = true

                if !rollingBackToCircle {
                    self.titleLabel.setAnimatedText(
                        localize("swype_det_circular"),
                        delay: 3.0)
                    self.swypeDirectionContainer.isVisible = false
                    self.roundMovementView.startAnimating()
                    self.roundMovementView.showAnimated() { [weak self] _ in
                        self?.isCompletingAnimation = false
                    }
                }
                else {
                    self.progressSw.setError()
                    self.progressSw.hideAnimated(duration: 1.0, delay: 0.5) { [weak self] _ in
                        self?.progressSw.reset()
                    }

                    self.swypeDirectionContainer.paintToErrorColor()
                    self.swypeDirectionContainer.hideAnimated(duration: 1.0, delay: 0.5) { [weak self] _ in
                        self?.titleLabel.setAnimatedText(localize("swype_det_wrong"), delay: 1.5)

                        self?.swypeDirectionContainer.resetAnimatedTargetPosition(animated: false)
                        self?.swypeDirectionContainer.restoreColors()

                        self?.roundMovementView.startAnimating()
                        self?.roundMovementView.showAnimated() { [weak self] _ in
                            self?.isCompletingAnimation = false
                        }
                    }
                }

            case .waitingToStartSwypeCode:
                self.titleLabel.setAnimatedText(localize("swype_det_stop"), delay: 0.80)
                self.isCompletingAnimation = true
                self.roundMovementView.hideAnimated() { [weak self] _ in
                    self?.progressSw.reset()
                    self?.progressSw.showAnimated()
                    self?.swypeDirectionContainer.showAnimated() { [weak self] _ in
                        self?.roundMovementView.stopAnimating()
                        self?.isCompletingAnimation = false
                    }
                }

            case .detectingSwypeCode:
                break

            case .swypeCodeDetected:
                
                self.titleLabel.setAnimatedText(localize("swype_det_success"), delay: 2.0)
                self.progressSw.setLastStep()
                self.isCompletingAnimation = true
                self.swypeDirectionContainer.resetAnimatedTargetPosition(animated: true) { [weak self] _ in
                    self?.swypeDirectionContainer.hideAnimated()
                    self?.progressSw.hideAnimated() { [weak self] _ in
                        self?.progressSw.reset()
                        self?.swypeEnteredImageView.showAnimated() { [weak self] _ in
                            self?.isCompletingAnimation = false

                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
                                guard let `self` = self else {
                                    return
                                }
                                
                                self.swypeEnteredViewCenterXConstraint.isActive = false
                                self.swypeEnteredViewCenterYConstraint.isActive = false
                                self.swypeEnteredViewCornerXConstraint.isActive = true
                                self.swypeEnteredViewCornerYConstraint.isActive = true
                                
                                self.swypeEnteredViewWidthConstraint.constant = 0.5 * self.swypeEnteredViewInitHeight
                                self.swypeEnteredViewHeightConstraint.constant = 0.5 * self.swypeEnteredViewInitHeight
                                
                                UIView.animate(withDuration: 0.5, animations: { [weak self] in
                                    self?.superview!.layoutIfNeeded()
                                })
                            }
                        }
                    }
                }
            }
            
            self.currentDetectorState = newState
        }
    }
    
    func discardMovement(_ completion: @escaping () -> Void) {
        self.roundMovementView.hideAnimated(duration: 0.5, delay: 0) { (_) in
            self.titleLabel.isHidden = true
            self.roundMovementView.stopAnimating()
            completion()
        }
    }
}

extension SwypeDetectorView {
    
    func update(from prevState: SwypeViewControllerState?, to newState: SwypeViewControllerState, isNotEnoughFunds: Bool) {

        print("[SwypeDetectorView] switch to \(newState)")

        switch newState {
        case .finishingRecord:
            swypeDirectionContainer.hideAnimated()
            progressSw.hideAnimated()
            roundMovementView.hideAnimated() { [weak self] _ in
                self?.roundMovementView.stopAnimating()
            }
            swypeEnteredImageView.hideAnimated() { [weak self] _ in
                guard let `self` = self else {
                    return
                }
                
                self.swypeEnteredViewCenterXConstraint.isActive = true
                self.swypeEnteredViewCenterYConstraint.isActive = true
                self.swypeEnteredViewCornerXConstraint.isActive = false
                self.swypeEnteredViewCornerYConstraint.isActive = false

                self.swypeEnteredViewWidthConstraint.constant = self.swypeEnteredViewInitWidth
                self.swypeEnteredViewHeightConstraint.constant = self.swypeEnteredViewInitHeight
            }

        case .submittingVideoHash:
            titleLabel.setAnimatedText(localize("swype_det_calc_hash"), delay: 0.80)
            
        case .requestingBalanceAndPriceOnAppearance:
            if let prevState = prevState, prevState == .submittingVideoHash {
                self.titleLabel.setAnimatedText(localize("swype_det_posted_to_blckchn"), delay: 0.80)
            }

        default:
            break
        }
    }
}
