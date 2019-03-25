import UIKit
import PromiseKit
import Moya
import Common

class ClapperboardViewController: BaseViewController, SuspendableStateMachine {
    
    // MARK: - IBOutlet
    @IBOutlet weak var proverRotatingView: SpinnerView!
    @IBOutlet weak var balanceLabel: BalanceLabel!
    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var qrTextField: QrTextField!
    @IBOutlet weak var qrButton: RoundButton!
    @IBOutlet weak var walletButton: UIButton!
    @IBOutlet weak var settingsButton: UIButton!
    @IBOutlet weak var guideButton: RoundButton!
    
    @IBOutlet weak var logoImageView: UIImageView!
    @IBOutlet weak var walletView: UIView!
    
    private var balance: Double = 0
    
    private func isNotEnoughFunds(price: Double) -> Bool {
        return price == 0 || balance < price
    }
    
    private var pendingDownloadingQRCode = false
    private var reachability: NetworkReachability!
    private var isStateSuspended: Bool = false
    
    private var submitMessageResponse: SubmitMessageResponse? {
        didSet {
            Settings.qrCodeData = submitMessageResponse?.qrCodeData
        }
    }
    
    private var controllerState: ClapperboardViewControllerState = .readyToSubmitMessage {
        didSet {
            updateState()
        }
    }
    private var testMode: Bool = false
    private var version = SharedSettings.shared.appVersion
    
    weak var qrViewController: QRCodeViewController!
    
    // MARK: - IBAction
    @IBAction func walletButtonAction(_ sender: UIButton) {
        let vc = UserPagesViewController()
        show(vc, sender: nil)
    }
    
    @IBAction func endInputText(_ sender: UITextField) {
    }
    
    // MARK: - Lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()
        
        reachability = NetworkReachability(stateMachine: self)
        
        setupLogo()
        setupQrTextField()
        setupQrButton()
        setupHelpButton()
        setupSettingsButton()
        
        setInitState()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        navigationController?.setNavigationBarHidden(true, animated: true)
        actionOnAppearance()
    }
    
    override func handleWillEnterForeground() {
        actionOnAppearance()
    }
    
    @objc private func actionOnAppearance() {
        switch controllerState {
        case .readyToSubmitMessage:
            controllerState = .gettingBalance
            getBalance()
            
        case .gettingBalance,
             .submittingMessage:
            proverRotatingView.startAnimation()
            
        case .showingCodeView,
             .showCodeView,
             .closingCodeView:
            break
        }
    }
    
    // To be called by self.reachability, see its type declaration for details
    func suspendState() {
        
        switch controllerState {
        case .gettingBalance:
            isStateSuspended = true
            
        case .readyToSubmitMessage,
             .submittingMessage,
             .showingCodeView,
             .showCodeView,
             .closingCodeView:
            isStateSuspended = false
        }
    }
    
    // To be called by self.reachability, see its type declaration for details
    func resumeState() {
        
        // If we are in .readyToSubmitMessage then we anyway refresh the balance
        guard isStateSuspended || controllerState == .readyToSubmitMessage else {
            return
        }
        
        isStateSuspended = false
        
        switch controllerState {
        case .readyToSubmitMessage:
            controllerState = .gettingBalance
            getBalance()
            
        case .gettingBalance:
            getBalance()
            
        case .submittingMessage,
             .showingCodeView,
             .showCodeView,
             .closingCodeView:
            break
        }
    }
    
    private func setInitState() {
        if Settings.qrCodeData != nil {
            controllerState = .showingCodeView
            showQRCodeView()
        }
        else {
            updateState()
        }
    }
    
    private func revertState() {
        
        switch controllerState {
        case .gettingBalance,
             .submittingMessage:
            controllerState = .readyToSubmitMessage
            
        case .readyToSubmitMessage,
             .showingCodeView,
             .showCodeView,
             .closingCodeView:
            break
        }
    }
    
    private func switchState() {
        switch controllerState {
        case .gettingBalance:
            controllerState = .readyToSubmitMessage
            
        case .readyToSubmitMessage:
            controllerState = .submittingMessage
            submitMessage()
            
        case .submittingMessage:
            controllerState = .showingCodeView
            showQRCodeView()
            
        case .showingCodeView:
            controllerState = .showCodeView
            
        case .showCodeView:
            controllerState = .closingCodeView
            closeQRCodeView()
            
        case .closingCodeView:
            controllerState = .gettingBalance
            getBalance()
        }
    }
    
    private func setupLogo() {
        let logoImage = version == .old ? #imageLiteral(resourceName: "Logo")  : #imageLiteral(resourceName: "logo_white")
        logoImageView.image = logoImage.resizedImage(newSize: logoImageView.frame.size)
    }
    
    private func setupQrTextField() {
        qrTextField.text = Settings.qrMessage
        qrTextField.addTarget(self, action: #selector(textFieldIsEditing), for: .editingChanged)
    }
    
    private func setupQrButton() {
        qrButton.imageView?.contentMode = .scaleAspectFit
        qrButton.backgroundColor = .white
        qrButton.isEnabled = !(qrTextField.text ?? "").isEmpty
        qrButton.addTarget(self, action: #selector(qrButtonAction), for: .touchUpInside)
    }
    
    private func setupHelpButton() {
        guideButton.tintColor = UIColor(hexString: "3B3D47")
        guideButton.setResizedImage(#imageLiteral(resourceName: "help"), for: .normal)
        guideButton.addTarget(self, action: #selector(guideButtonAction), for: .touchUpInside)
    }
    
    private func setupSettingsButton() {
        setupSettingsButtonImage()
        settingsButton.addTarget(self, action: #selector(settingsButtonAction), for: .touchUpInside)
    }
    
    private func setupSettingsButtonImage(_ qrCodeState: Bool = false) {
        let color: UIColor = qrCodeState ? .white : UIColor(hexString: "3B3D47")
        settingsButton.tintColor = color
        settingsButton.imageView?.contentMode = .center
        let image = #imageLiteral(resourceName: "settings").resizedImage(newSize: CGSize(width: 20, height: 20))
        
        settingsButton.setImage(image, for: .normal)
    }
    
    @objc private func guideButtonAction(_ sender: UIButton) {
        let guideViewController = UserGuideViewController()
        show(guideViewController, sender: nil)
    }
    
    @objc private func settingsButtonAction(_ sender: UIButton) {
        let userPagesViewController = UserPagesViewController()
        userPagesViewController.openSettings = true
        show(userPagesViewController, sender: nil)
    }
    
    @objc private func qrButtonAction(_ sender: UIButton) {
        switchState()
    }
    
    private func updateState() {
        
        balanceLabel.update(balance: balance)
        statusLabel.isHidden = false
        
        switch controllerState {
        case .readyToSubmitMessage:
            qrTextField.isEnabled = true
            
        case .gettingBalance,
             .submittingMessage,
             .showingCodeView,
             .showCodeView,
             .closingCodeView:
            qrTextField.isEnabled = false
        }
        
        switch controllerState {
        case .readyToSubmitMessage:
            qrButton.isEnabled = !(qrTextField.text ?? "").isEmpty
            
        case .showCodeView:
            qrButton.isEnabled = true
            
        case .gettingBalance,
             .submittingMessage,
             .showingCodeView,
             .closingCodeView:
            qrButton.isEnabled = false
        }
        
        switch controllerState {
        case .gettingBalance:
            statusLabel.text = "Refreshing balance"
            
        case .submittingMessage:
            statusLabel.text = "Submitting message"
            
        case .readyToSubmitMessage,
             .showingCodeView,
             .showCodeView,
             .closingCodeView:
            statusLabel.text = " "
        }
        
        let qrButtonImageName: String
        
        switch controllerState {
        case .showCodeView, .closingCodeView:
            qrButtonImageName = "close"
            
        case .gettingBalance,
             .readyToSubmitMessage,
             .submittingMessage,
             .showingCodeView:
            qrButtonImageName = "qr"
        }
        
        qrButton.setImage(#imageLiteral(resourceName: qrButtonImageName).resizedImage(newSize: qrButton.frame.size), for: .normal)
    }
}

// MARK: - QRCode actions
extension ClapperboardViewController {
    private func showQRCodeView() {
        guard let qrView: QRCodeViewController = UIStoryboard.initViewController(fromStoryboard: "Main", withName: "qrCodeViewController") else { return }
        
        qrViewController = qrView
        
        view.bringSubviewToFront(qrButton)
        addChild(qrViewController)
        qrViewController.didMove(toParent: self)
        
        UIView.transition(with: view, duration: 0.3, options: .transitionCrossDissolve, animations: {
            self.view.insertSubview(self.qrViewController.view, belowSubview: self.qrButton)
            self.walletView.isHidden = true
            self.logoImageView.isHidden = false
            
            if let superView = self.walletView.superview {
                self.view.bringSubviewToFront(superView)
            }
            if let superView = self.settingsButton.superview {
                self.setupSettingsButtonImage(self.version == .new)
                self.view.bringSubviewToFront(superView)
            }
        }, completion: { _ in
            self.qrViewController.view.translatesAutoresizingMaskIntoConstraints = false
            self.qrViewController.view.bindToEdges(ofView: self.view)
            
            self.qrViewController.generateQRCode(message: self.qrTextField.text!, qrCodeData: Settings.qrCodeData!) { [weak self] in
                self?.switchState()
            }
        })
    }
    
    // Text Mode
    private func showTestQRCodeView() {
        guard let qrView: QRCodeViewController = UIStoryboard.initViewController(fromStoryboard: "Main", withName: "qrCodeViewController") else { return }
        
        qrViewController = qrView
        
        view.bringSubviewToFront(qrButton)
        addChild(qrViewController)
        qrViewController.didMove(toParent: self)
        
        UIView.transition(with: view, duration: 0.3, options: .transitionCrossDissolve, animations: {
            self.view.insertSubview(self.qrViewController.view, belowSubview: self.qrButton)
            self.walletView.isHidden = true
            self.logoImageView.isHidden = false
            
            if let superView = self.walletView.superview {
                self.view.bringSubviewToFront(superView)
            }
            if let superView = self.settingsButton.superview {
                self.setupSettingsButtonImage(self.version == .new)
                self.view.bringSubviewToFront(superView)
            }
        }, completion: { _ in
            self.qrViewController.view.translatesAutoresizingMaskIntoConstraints = false
            self.qrViewController.view.bindToEdges(ofView: self.view)
            
            self.qrViewController.generateTestQR { [weak self] in
                self?.switchState()
            }
        })
    }
    
    private func closeQRCodeView() {
        navigationController?.setNavigationBarHidden(true, animated: false)
        
        submitMessageResponse = nil
        setupSettingsButtonImage()
        
        qrViewController.willMove(toParent: self)
        
        UIView.transition(with: view, duration: 0.3, options: .transitionCrossDissolve, animations: {
            self.qrViewController.view.removeFromSuperview()
            self.walletView.isHidden = false
            self.logoImageView.isHidden = true
        }, completion: { _ in
            self.qrViewController.removeFromParent()
            self.switchState()
        })
    }
}

// MARK: - UITextFieldDelegate

extension ClapperboardViewController {
    @objc func textFieldIsEditing(_ sender: UITextField) {
        qrButton.isEnabled = !(sender.text ?? "").isEmpty
    }
}

// MARK: - Network layer
extension ClapperboardViewController {
    
    private func getBalance() {
        
        proverRotatingView.startAnimation()
        
        provider.promise(target: NetworkAPI.get_balance)
            .done { [weak self] (result: BalanceResponse) in
                self?.proverRotatingView.stopAnimation()
                
                let balanceItem = result.balance.first(where: {
                    $0.mosaicId.namespaceId == .prover
                })
                
                self?.balance = balanceItem?.floatQuantity ?? 0
                self?.switchState()
            }
            .catch { [weak self] (error: Error) in
                if !(self?.isStateSuspended ?? false) {
                    self?.proverRotatingView.stopAnimation()
                    self?.revertState()
                    self?.requestFailed(error)
                }
        }
    }
    
    private func estimateSubmissionFee() {
        
        proverRotatingView.startAnimation()
        
        let provider = self.provider
        
        provider.promise(target: NetworkAPI.get_balance)
            .then { [weak self] (result: BalanceResponse) -> Promise<[QuantityItem]> in
                
                let balanceItem = result.balance.first(where: {
                    $0.mosaicId.namespaceId == .prover
                })
                
                self?.balance = balanceItem?.floatQuantity ?? 0
                
                let message = self?.qrTextField.text! ?? ""
                let estimateFeeEntry = NetworkAPI.estimate_submit_message_fee(message: message)
                
                return provider.promise(target: estimateFeeEntry)
            }
            .done { [weak self] (result: [QuantityItem]) in
                
                let quantityItem = result.first(where: {
                    $0.mosaicId.namespaceId == .prover
                })
                
                let price = quantityItem?.floatQuantity ?? 0
                let isNotEnoughFunds = self?.isNotEnoughFunds(price: price) ?? false
                
                if isNotEnoughFunds {
                    self?.proverRotatingView.stopAnimation()
                    self?.revertState()
                }
                else {
                    self?.switchState()
                }
            }
            .catch { [weak self] (error: Error) in
                self?.proverRotatingView.stopAnimation()
                self?.revertState()
                self?.requestFailed(error)
        }
    }
    
    private func submitMessage() {
        
        Settings.qrMessage = qrTextField.text!
        
        proverRotatingView.startAnimation()
        
        let submitMessageEntry = NetworkAPI.submit_message(message: qrTextField.text!)
        
        provider.promise(target: submitMessageEntry)
            .done { [weak self] (result: SubmitMessageResponse) in
                self?.submitMessageResponse = result
                self?.switchState()
            }
            .catch { [weak self] (error: Error) in
                self?.proverRotatingView.stopAnimation()
                self?.revertState()
                self?.requestFailed(error)
        }
    }
    
    private func confirmMessageSubmission() {
        
        let confirmEntry = NetworkAPI.confirm_message_submission(txHash: submitMessageResponse!.txhash)
        
        provider.promise(target: confirmEntry)
            .done { [weak self] (result: ConfirmSubmissionResponse) in
                guard result.height != nil else {
                    self?.pendingDownloadingQRCode = true
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) { [weak self] in
                        self?.pendingDownloadingQRCode = false
                        self?.confirmMessageSubmission()
                    }
                    return
                }
                
                self?.proverRotatingView.stopAnimation()
                self?.switchState()
            }
            .catch { [weak self] (error: Error) in
                let urlErrorCode = (error as? MoyaError)?.urlError?.urlErrorCode
                
                guard urlErrorCode == .notConnectedToInternet else {
                    self?.proverRotatingView.stopAnimation()
                    self?.revertState()
                    self?.requestFailed(error)
                    return
                }
                
                if !(self?.isStateSuspended ?? false) {
                    self?.confirmMessageSubmission()
                }
        }
    }
}
