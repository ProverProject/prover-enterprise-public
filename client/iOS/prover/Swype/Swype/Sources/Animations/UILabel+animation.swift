import Foundation
import UIKit

extension UILabel {
    func setAnimatedText(_ text: String?, duration: TimeInterval = 0.50, delay: TimeInterval = 3) {
        
        self.text = text
        
        DispatchQueue.main.async {
            UIView.transition(
                with: self, duration: duration, options: .transitionCrossDissolve,
                animations: { [weak self] in
                    self?.isHidden = false
                },
                completion: { _ in
                    DispatchQueue.main.asyncAfter(deadline: .now() + delay) { [weak self] in
                        guard let `self` = self else { return }
                        UIView.transition(
                            with: self, duration: duration, options: .transitionCrossDissolve,
                            animations: { [weak self] in
                                self?.isHidden = true
                        })
                    }
                })
        }

        /*
        UIView.animate(withDuration: duration, animations: { [weak self] in
            self?.alpha = 0
        }, completion: { _ in
            UIView.animate(withDuration: duration, animations: { [weak self] in
                self?.text = text
                self?.alpha = 1
            })
        })

        DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
            UIView.animate(withDuration: duration, animations: { [weak self] in
                self?.alpha = 0
            }, completion: { [weak self] _ in
                self?.text = " "
            })
        }
         */
    }
}
