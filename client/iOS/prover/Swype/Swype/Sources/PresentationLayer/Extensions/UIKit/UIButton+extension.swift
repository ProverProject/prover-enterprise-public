import UIKit

extension UIButton {
    
    func setImage(image: UIImage) {
        DispatchQueue.main.async { [weak self] in
            self?.setImage(image, for: .normal)
        }
    }
    
    func setEnable(_ isEnabled: Bool) {
        DispatchQueue.main.async { [weak self] in
            self?.isEnabled = isEnabled
        }
    }
}
