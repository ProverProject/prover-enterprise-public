import UIKit

class LicenseContainer: BaseNibView {
    
    @IBOutlet weak var licenseNumberLabel: UILabel!    
    @IBOutlet weak var copyButton: UIButton!

    private var alertingViewController: AlertingViewController?

    override func commonSetup() {
        self.setupLicenseLabel()
        self.setupCopyButton()
    }

    private func setupLicenseLabel() {
        self.licenseNumberLabel.numberOfLines = 2
        self.licenseNumberLabel.font = .systemFont(ofSize: 12.0, weight: UIFont.Weight.thin)
        self.licenseNumberLabel.sizeToFit()
    }
    
    private func setupCopyButton() {
        self.copyButton.tintColor = CoreColors.darkGray.color
        self.copyButton.setImage(#imageLiteral(resourceName: "copy"), for: .normal)
        self.copyButton.addTarget(self, action: #selector(copyButtonAction), for: .touchUpInside)
    }
    
    public func setWallet(with text: String) {
        self.licenseNumberLabel.text = text
    }

    public func setAlertingViewController(_ alertingViewController: AlertingViewController) {
        self.alertingViewController = alertingViewController
    }

    @objc
    private func copyButtonAction() {
        UIPasteboard.general.string = self.licenseNumberLabel.text
        alertingViewController?.alert("The license ID has been copied")
    }
}
