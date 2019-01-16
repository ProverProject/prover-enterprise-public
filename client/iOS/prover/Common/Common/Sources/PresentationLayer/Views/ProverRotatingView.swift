import UIKit

public class ProverRotatingView: UIView, CAAnimationDelegate {

    private func byteColor(red: UInt8, green: UInt8, blue: UInt8, alpha: UInt8) -> UIColor {
        return UIColor(red: CGFloat(red) / 255,
                       green: CGFloat(green) / 255,
                       blue: CGFloat(blue) / 255,
                       alpha: CGFloat(alpha) / 100)
    }

    private var arcBlackColor: UIColor {
        return byteColor(red:  16, green: 20, blue: 39, alpha: 100)
    }

    private var arcRedColor: UIColor {
        return byteColor(red: 227, green: 30, blue: 36, alpha: 100)
    } 

    private var shapeLayer: CAShapeLayer!

    private let rotatingAnimationKey = "rotatingAnimation"
    private let pathAnimationKey = "pathAnimation"

    public override func awakeFromNib() {
        super.awakeFromNib()

        shapeLayer = CAShapeLayer()
        shapeLayer.fillColor = UIColor.clear.cgColor
        shapeLayer.lineWidth = 4
        shapeLayer.strokeColor = arcRedColor.cgColor

        shapeLayer.strokeStart = 0
        shapeLayer.strokeEnd = 1

        let center = CGPoint(x: bounds.midX, y: bounds.midY)
        let bezierPath = UIBezierPath()

        bezierPath.addArc(withCenter: center, radius: 18, startAngle: .pi*(-0.5), endAngle: .pi*1.5, clockwise: true)

        shapeLayer.path = bezierPath.cgPath

        layer.addSublayer(shapeLayer)
    }

    public func startAnimation() {
        let pathAnimation = CABasicAnimation(keyPath: "strokeStart")
        
        pathAnimation.delegate = self
        pathAnimation.fromValue = shapeLayer.strokeStart
        pathAnimation.toValue = CGFloat(0.75)
        pathAnimation.duration = 1
        
        shapeLayer.add(pathAnimation, forKey: pathAnimationKey)
        
        let rotationAnimation = CABasicAnimation(keyPath: "transform.rotation")

        rotationAnimation.delegate = self
        rotationAnimation.fromValue = CGFloat(0)
        rotationAnimation.toValue = CGFloat.pi*2
        rotationAnimation.duration = 1.5
        rotationAnimation.repeatCount = Float.greatestFiniteMagnitude

        layer.add(rotationAnimation, forKey: rotatingAnimationKey)
    }

    public func animationDidStop(_ anim: CAAnimation, finished flag: Bool) {
        let basicAnimation = anim as! CABasicAnimation
        let keyPath = basicAnimation.keyPath

        guard keyPath == "strokeStart" else { return }
        guard flag else { return }

        let newValue = basicAnimation.toValue as! CGFloat

        shapeLayer.strokeStart = newValue
    }

    public func stopAnimation() {
        shapeLayer.removeAllAnimations()
        layer.removeAllAnimations()

        shapeLayer.strokeStart = 0
    }
}
