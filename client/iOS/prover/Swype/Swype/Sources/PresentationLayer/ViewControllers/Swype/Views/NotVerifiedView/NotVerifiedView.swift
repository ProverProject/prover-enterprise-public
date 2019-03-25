import UIKit
import Common

class NotVerifiedView: NibView {
    
    @IBOutlet private weak var messageLabel: UILabel!
    @IBOutlet private weak var imageView: UIImageView!
    
    override func commonSetup() {
        self.setupLayout()
    }
    
    private func setupLayout() {
        self.messageLabel.text = localize("not_verified_lbl")
        
        self.imageView.tintColor = .white
        self.imageView.image = #imageLiteral(resourceName: "swype_discarded").resizedImage(newSize: self.imageView.frame.size).withRenderingMode(.alwaysTemplate)
    }
}
