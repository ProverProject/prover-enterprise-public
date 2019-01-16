import UIKit

class QRCodeView: BaseNibView {
    private let version = SharedSettings.shared.appVersion
    private let placeholderImage = #imageLiteral(resourceName: "symbol")
    
    @IBOutlet weak var qrImageView: UIImageView!
    @IBOutlet weak var logoQrImageView: UIImageView!
    @IBOutlet weak var maskImageView: UIImageView!
    
    override func commonSetup() {
        self.setupImageViews()
    }
    
    private func setupImageViews() {
        self.qrImageView.contentMode = .scaleAspectFit
        self.qrImageView.tintColor = version == .new ? .white : UIColor(hexString: "3B3D47")
        
        self.logoQrImageView.contentMode = .scaleAspectFit
        self.logoQrImageView.tintColor = .red
        
        self.maskImageView.image = placeholderImage
        self.maskImageView.contentMode = .center
    }
    
    public func setQrImage(_ image: UIImage?) {
        self.qrImageView.image = image?.withRenderingMode(.alwaysTemplate)
        self.logoQrImageView.image = image?.withRenderingMode(.alwaysTemplate)
        self.maskImageView.frame = self.logoQrImageView.bounds
        self.logoQrImageView.mask = self.maskImageView
    }
}
