import UIKit

class SwypeDirectionCenterView: UIView {

    private let lineWidth: CGFloat = 4
    private let radius: CGFloat = 30
    private var savedBounds: CGRect = .zero

    @IBInspectable var strokeColor: UIColor? {
        get {
            guard let sc = layer.strokeColor else {
                return nil
            }
            return UIColor(cgColor: sc)
        }
        set {
            layer.strokeColor = newValue?.cgColor
        }
    }

    override var intrinsicContentSize: CGSize {
        return CGSize(width: radius, height: 2 * radius)
    }

    override var layer: CAShapeLayer {
        return super.layer as! CAShapeLayer
    }

    override class var layerClass: AnyClass {
        return CAShapeLayer.self
    }

    required override init(frame: CGRect) {
        super.init(frame: frame)
        setup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        setup()
    }

    private func setup() {
        setContentCompressionResistancePriority(UILayoutPriority(950), for: .horizontal)
        
        layer.fillColor = nil
        layer.lineWidth = lineWidth
    }

    override func layoutSubviews() {
        super.layoutSubviews()

        guard savedBounds != bounds else {
            return
        }

        layer.path = createPath()
        savedBounds = bounds
    }

    private func createPath() -> CGPath {

        let rect = CGRect(x: -radius, y: 0, width: 2 * radius, height: 2 * radius)
        return UIBezierPath(ovalIn: rect.insetBy(dx: lineWidth / 2, dy: lineWidth / 2)).cgPath
    }
}
