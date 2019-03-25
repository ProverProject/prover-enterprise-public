import Foundation
import UIKit

extension UIView {

    var isVisible: Bool {
        get {
            return !isHidden
        }
        set {
            isHidden = !newValue
        }
    }

    func hideAnimated(duration: TimeInterval = 0.5, delay: TimeInterval = 0, completion: ((Bool) -> Void)? = nil) {
        setAnimatedVisibility(isHidden: true, duration: duration, delay: delay, completion: completion)
    }

    func showAnimated(duration: TimeInterval = 0.5, delay: TimeInterval = 0, completion: ((Bool) -> Void)? = nil) {
        setAnimatedVisibility(isHidden: false, duration: duration, delay: delay, completion: completion)
    }

    private func setAnimatedVisibility(isHidden: Bool, duration: TimeInterval, delay: TimeInterval, completion: ((Bool) -> Void)?) {
        DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
            UIView.transition(
                with: self, duration: duration, options: .transitionCrossDissolve,
                animations: { [weak self] in
                    self?.isHidden = isHidden
                },
                completion: completion)
        }
    }
}
