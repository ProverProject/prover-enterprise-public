import Foundation
import UIKit

class TargetContainerView: UIView {

    @IBOutlet private weak var ringView: UIImageView!    
    @IBOutlet private weak var targetView: UIImageView!
    @IBOutlet private weak var targetViewX: NSLayoutConstraint!
    @IBOutlet private weak var targetViewY: NSLayoutConstraint!
    
    private var swypeDirectionLayer: CAShapeLayer!

    public var targetPosition: (x: CGFloat, y: CGFloat) {
        get {
            return (targetViewX.constant, targetViewY.constant)
        }
        set {
            (targetViewX.constant, targetViewY.constant) = (newValue.x, newValue.y)
        }
    }

    public var path: CGPath? {
        get {
            return swypeDirectionLayer.path
        }
        set {
            swypeDirectionLayer.path = newValue
        }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        
        swypeDirectionLayer = CAShapeLayer()
        
        swypeDirectionLayer.strokeColor = UIColor(white: 1, alpha: 0.25).cgColor
        swypeDirectionLayer.fillColor = UIColor.clear.cgColor
        swypeDirectionLayer.lineWidth = 2
        swypeDirectionLayer.lineJoin = CAShapeLayerLineJoin.round
        swypeDirectionLayer.lineDashPattern = [5, 2]
        
        layer.addSublayer(swypeDirectionLayer)
    }
}
