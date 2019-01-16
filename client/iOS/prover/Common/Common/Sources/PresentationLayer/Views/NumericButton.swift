import UIKit

open class NumericButton: UIButton {
    override open var isHighlighted: Bool {
        didSet {
            backgroundColor = isHighlighted ? UIColor(white: 0.95, alpha: 1) : .white
        }
    }
}
