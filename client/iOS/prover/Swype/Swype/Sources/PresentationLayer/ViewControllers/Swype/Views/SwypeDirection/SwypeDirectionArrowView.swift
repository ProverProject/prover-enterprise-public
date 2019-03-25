import UIKit

class SwypeDirectionArrowView: UIView {

    private let lineWidth: CGFloat = 2
    private let width: CGFloat = 8
    private let height: CGFloat = 16
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
        return CGSize(width: width, height: height)
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
        layer.fillColor = nil
        layer.lineWidth = lineWidth
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()

        guard savedBounds != bounds else {
            return
        }

        if bounds.width >= width {
            layer.path = createPath()
        }
        else {
            layer.path = nil
        }

        savedBounds = bounds
    }
    
    private func createPath() -> CGPath {

        let path = CGMutablePath()

        path.move(to: CGPoint(x: 0, y: 0))
        path.addLine(to: CGPoint(x: bounds.width, y: 0.5 * bounds.height))
        path.addLine(to: CGPoint(x: 0, y: bounds.height))

        return path
    }
}
