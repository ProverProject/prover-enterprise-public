import UIKit

open class LicenseViewController: BaseViewController {
    
    private lazy var licenseLabel: UILabel = {
        let lazyLicenseLabel = UILabel()
        lazyLicenseLabel.text = "License number"
        lazyLicenseLabel.sizeToFit()
        lazyLicenseLabel.numberOfLines = 0
        lazyLicenseLabel.textColor = CoreColors.lightGray.color
        lazyLicenseLabel.font = .systemFont(ofSize: 13.0)
        lazyLicenseLabel.translatesAutoresizingMaskIntoConstraints = false
        return lazyLicenseLabel
    }()
    
    private lazy var qrCodeContainer: QrCodeContainer = {
        let lazyContainer = QrCodeContainer()
        lazyContainer.translatesAutoresizingMaskIntoConstraints = false
        return lazyContainer
    }()
    
    override open func loadView() {
        super.loadView()
        
        self.view.addSubview(self.licenseLabel)
        self.view.addSubview(self.qrCodeContainer)
        
        self.configureLayout()
        
        self.view.setNeedsLayout()
        self.view.layoutIfNeeded()
    }
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        
        self.commonSetup()
    }

    private func commonSetup() {
        self.view.backgroundColor = .white
        self.qrCodeContainer.setNumber("NBPDEYVZ5UTTGNC2NHS5NBPDEYVZ5UTTGNC2NHS5")
        self.qrCodeContainer.setAlertingViewController(self)
    }
    
    private func configureLayout() {
        NSLayoutConstraint.activate([
            self.licenseLabel.topAnchor.constraint(equalTo: self.view.topAnchor, constant: 16),
            self.licenseLabel.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 16),
            //self.view.trailingAnchor.constraint(equalTo: self.licenseLabel.trailingAnchor, constant: 16),
            
            self.qrCodeContainer.topAnchor.constraint(equalTo: self.licenseLabel.bottomAnchor, constant: 32),
            self.qrCodeContainer.centerXAnchor.constraint(equalTo: self.view.centerXAnchor),
            self.qrCodeContainer.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 40),
            //self.qrCodeContainer.trailingAnchor.constraint(equalTo: self.view.trailingAnchor, constant: -40)
            //self.view.bottomAnchor.constraint(greaterThanOrEqualTo: self.qrCodeContainer.bottomAnchor)
            ])
    }
}
