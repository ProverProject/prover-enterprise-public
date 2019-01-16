import UIKit

class QrTextField: UITextField {
    override var isEnabled: Bool {
        didSet {
            self.textColor = isEnabled ? . black : .gray
        }
    }
}
