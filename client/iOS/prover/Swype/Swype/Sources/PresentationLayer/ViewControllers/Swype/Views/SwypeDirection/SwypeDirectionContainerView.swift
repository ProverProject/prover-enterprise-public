import UIKit

class SwypeDirectionContainerView: UIView {

    @IBOutlet private weak var widthConstraint: NSLayoutConstraint!
    @IBOutlet private weak var heightConstraint: NSLayoutConstraint!

    @IBOutlet private weak var stackView: UIStackView!
    
    private var originalArrowViewColors: [UIColor?] = []
    private var originalCenterViewColors: [UIColor?] = []
    private var originalTargetViewColors: [UIColor?] = []

    var targetPosition: (x: CGFloat, y: CGFloat) = (0, 0) {
        didSet {
            let size = 2 * hypot(targetPosition.x, targetPosition.y)
            
            widthConstraint.constant = size
            heightConstraint.constant = size
            
            let rotationAngle = atan2(targetPosition.y, targetPosition.x)
            transform = CGAffineTransform(rotationAngle: rotationAngle)
        }
    }
    
    override func awakeFromNib() {
        let arrangedSubviews = stackView.arrangedSubviews
        
        originalArrowViewColors = arrangedSubviews.compactMap {
            $0 as? SwypeDirectionArrowView
        }.map { $0.strokeColor }
        
        originalCenterViewColors = arrangedSubviews.compactMap {
            $0 as? SwypeDirectionCenterView
        }.map { $0.strokeColor }
        
        originalTargetViewColors = arrangedSubviews.compactMap {
            $0 as? SwypeDirectionTargetView
        }.map { $0.strokeColor }
    }

    func resetAnimatedTargetPosition(animated: Bool, completion: ((Bool) -> Void)? = nil) {
        self.widthConstraint.constant = 0
        self.heightConstraint.constant = 0

        if animated {
            UIView.animate(withDuration: 0.30, animations: { [weak self] in
                self?.layoutIfNeeded()
            }, completion: completion)
        }
    }
    
    func paintToErrorColor() {
        let arrangedSubviews = stackView.arrangedSubviews

        let arrowViews = arrangedSubviews.compactMap { $0 as? SwypeDirectionArrowView }
        let centerViews = arrangedSubviews.compactMap { $0 as? SwypeDirectionCenterView }
        let targetViews = arrangedSubviews.compactMap { $0 as? SwypeDirectionTargetView }

        for view in arrowViews {
            view.strokeColor = .red
        }

        for view in centerViews {
            view.strokeColor = .red
        }

        for view in targetViews {
            view.strokeColor = .red
        }
    }
    
    func restoreColors() {
        let arrangedSubviews = stackView.arrangedSubviews

        let arrowViews = arrangedSubviews.compactMap { $0 as? SwypeDirectionArrowView }
        let centerViews = arrangedSubviews.compactMap { $0 as? SwypeDirectionCenterView }
        let targetViews = arrangedSubviews.compactMap { $0 as? SwypeDirectionTargetView }

        for (view, color) in zip(arrowViews, originalArrowViewColors) {
            view.strokeColor = color
        }
        
        for (view, color) in zip(centerViews, originalCenterViewColors) {
            view.strokeColor = color
        }
        
        for (view, color) in zip(targetViews, originalTargetViewColors) {
            view.strokeColor = color
        }
    }
}
