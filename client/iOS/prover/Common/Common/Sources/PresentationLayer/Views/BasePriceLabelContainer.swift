import UIKit

open class BasePriceLabelContainer: UIView {
    override open func layoutSubviews() {
        super.layoutSubviews()
        layer.cornerRadius = frame.height * 0.5
    }
}
