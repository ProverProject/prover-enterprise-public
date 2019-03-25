import UIKit

class GalleryButton: UIButton {

    override func awakeFromNib() {
        super.awakeFromNib()

        clipsToBounds = true

        layer.borderWidth = 2
        layer.borderColor = UIColor.white.cgColor
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        layer.cornerRadius = bounds.width / 2
    }
}
