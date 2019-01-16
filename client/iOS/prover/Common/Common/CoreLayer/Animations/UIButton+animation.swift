import Foundation
import UIKit

public extension UIButton {
    public func startRotating() {
        UIView.animate(withDuration: 0.1,
                       delay: 0,
                       options: [.repeat, .curveLinear],
                       animations: { [unowned self] in
                           self.transform = CGAffineTransform(rotationAngle: .pi)
                       })
    }
    
    public func stopRotating() {
        UIView.animate(withDuration: 0.1,
                       delay: 0,
                       options: [.beginFromCurrentState, .curveLinear],
                       animations: { [unowned self] in
                           self.transform = CGAffineTransform.identity
                       })
    }
}
