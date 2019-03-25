import UIKit

extension UILabel {
    
    func setText(_ text: String) {
        DispatchQueue.main.async { [weak self] in
            self?.text = text
        }
    }
}
