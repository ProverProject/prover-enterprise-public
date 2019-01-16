import Foundation
import UIKit

extension UIView {

    func setAnimatedVisibility(visible: Bool, completion: ((Bool) -> Void)? = nil) {
        UIView.transition(
                with: self, duration: 0.5, options: .transitionCrossDissolve,
                animations: { [unowned self] in
                    self.isHidden = !visible
                },
                completion: completion)
    }
}
