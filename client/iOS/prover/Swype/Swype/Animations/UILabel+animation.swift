import Foundation
import UIKit

extension UILabel {
    func setAnimatedText(_ text: String) {
        let duration: TimeInterval = 0.20
        
        UIView.animate(withDuration: duration, animations: { [unowned self] in
            self.alpha = 0
        }, completion: { _ in
            UIView.animate(withDuration: duration, animations: { [unowned self] in
                self.text = text
                self.alpha = 1
            })
        })

        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            UIView.animate(withDuration: duration, animations: { [unowned self] in
                self.alpha = 0
            }, completion: { [unowned self] _ in
                self.text = " "
            })
        }
    }
}
