import UIKit

class QrCodeContainer: UIView {
    
    private let appVerison = SharedSettings.shared.appVersion
    
    private let qrCodeView: QRCodeView = {
        let lazyQrCodeView = QRCodeView()
        lazyQrCodeView.translatesAutoresizingMaskIntoConstraints = false
        return lazyQrCodeView
    }()
    
    private lazy var walletContainer: LicenseContainer = {
        let lazyBottomContainer = LicenseContainer()
        lazyBottomContainer.translatesAutoresizingMaskIntoConstraints = false
        return lazyBottomContainer
    }()
    
    private lazy var qrCodeGenerator: QRGeneratorService = {
        let backColor: UIColor = appVerison == .new ? .black : .clear
        let frontColor: UIColor = appVerison == .new ? .clear : .black
        
        let qrGenerator = QRGeneratorService(backColor: backColor, frontColor: frontColor)
        return qrGenerator
    }()
    
    override class var requiresConstraintBasedLayout: Bool {
        return true
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.commonSetup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        self.commonSetup()
    }

    private func commonSetup() {
        self.addSubview(self.qrCodeView)
        self.addSubview(self.walletContainer)
        
        self.configureLayout()
    }
    
    private func configureLayout() {
        let qrCodeSide: CGFloat = 200
        
        NSLayoutConstraint.activate([
            self.qrCodeView.topAnchor.constraint(equalTo: self.topAnchor),
            self.qrCodeView.leadingAnchor.constraint(equalTo: self.leadingAnchor),
            self.qrCodeView.trailingAnchor.constraint(equalTo: self.trailingAnchor),
            self.qrCodeView.heightAnchor.constraint(equalTo: self.qrCodeView.widthAnchor, multiplier: 1.0/1.0),
            
            self.walletContainer.heightAnchor.constraint(equalToConstant: 46),
            self.walletContainer.topAnchor.constraint(equalTo: self.qrCodeView.bottomAnchor, constant: 32),
            self.walletContainer.leadingAnchor.constraint(equalTo: self.leadingAnchor),
            self.walletContainer.trailingAnchor.constraint(equalTo: self.trailingAnchor),
            self.walletContainer.bottomAnchor.constraint(equalTo: self.bottomAnchor)
            ])
    }
    
    public func setNumber(_ license: String) {        
        let qrImage = qrCodeGenerator.generateQRCode(fromString: license, withImageSize: self.qrCodeView.frame.size)
        
        self.qrCodeView.setQrImage(qrImage)
        self.walletContainer.setWallet(with: license)
    }

    public func setAlertingViewController(_ alertingViewController: AlertingViewController) {
        self.walletContainer.setAlertingViewController(alertingViewController)
    }
}
