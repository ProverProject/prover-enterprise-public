import UIKit

open class SecureTextButton: UIButton {
    override open var isSelected: Bool {
        didSet {
            self.transform = isSelected ? CGAffineTransform(scaleX: 1.2, y: 1.2) : CGAffineTransform(scaleX: 1, y: 1)
            self.backgroundColor = isSelected ? UIColor.red.withAlphaComponent(0.6) : .lightGray
        }
    }
}
