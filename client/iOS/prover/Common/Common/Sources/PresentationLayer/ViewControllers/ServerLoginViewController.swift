import UIKit
import TPKeyboardAvoiding

class ServerLoginViewController: BaseViewController {
    private lazy var scrollView: TPKeyboardAvoidingScrollView = {
        let lazyScrollView = TPKeyboardAvoidingScrollView(frame: self.view.frame)
        lazyScrollView.translatesAutoresizingMaskIntoConstraints = false
        return lazyScrollView
    }()
    
    private lazy var contentView: UIView = {
        let lazyContentView = UIView(frame: self.view.frame)
        lazyContentView.translatesAutoresizingMaskIntoConstraints = false
        return lazyContentView
    }()
    
    private lazy var titleImageView: UIImageView = {
        let lazyTitleImageView = UIImageView(image: #imageLiteral(resourceName: "prover_title"))
        lazyTitleImageView.contentMode = .scaleAspectFit
        lazyTitleImageView.translatesAutoresizingMaskIntoConstraints = false
        return lazyTitleImageView
    }()

    private lazy var credentialsLabel: UILabel = {
        let lazyCredentialsLabel = UILabel()
        lazyCredentialsLabel.text = Utils.localizeSelf("server_enter_address")
        lazyCredentialsLabel.numberOfLines = 0
        lazyCredentialsLabel.textAlignment = .center
        lazyCredentialsLabel.textColor = CoreColors.lightGray.color
        lazyCredentialsLabel.font = .systemFont(ofSize: 16.0)
        lazyCredentialsLabel.translatesAutoresizingMaskIntoConstraints = false
        return lazyCredentialsLabel
    }()
    
    private lazy var urlContainer: TextContainer = {
        let lazyUrlContainer = TextContainer()
        lazyUrlContainer.translatesAutoresizingMaskIntoConstraints = false
        return lazyUrlContainer
    }()
    
    private lazy var guideButton: UIButton = {
        let lazyGuideButton = UIButton(type: .system)
        lazyGuideButton.setTitle(Utils.localizeSelf("server_learn_how_to_use_app"), for: .normal)
        lazyGuideButton.setTitleColor(CoreColors.darkGray.color, for: .normal)
        lazyGuideButton.titleLabel?.font = UIFont(name: "Helvetica", size: 12.0)
        lazyGuideButton.titleLabel?.textAlignment = .center
        lazyGuideButton.addTarget(self, action: #selector(goToGuide), for: .touchUpInside)
        lazyGuideButton.translatesAutoresizingMaskIntoConstraints = false
        return lazyGuideButton
    }()
    
    private lazy var logInButton: UIButton = {
        let lazyLogInButton = UIButton(type: .system)
        lazyLogInButton.layer.cornerRadius = 2.0
        lazyLogInButton.layer.shouldRasterize = false
        lazyLogInButton.backgroundColor = CoreColors.darkGray.color
        lazyLogInButton.setTitle(Utils.localizeSelf("server_login_btn"), for: .normal)
        lazyLogInButton.setTitleColor(.white, for: .normal)
        lazyLogInButton.titleLabel?.font = .systemFont(ofSize: 14.0)
        lazyLogInButton.translatesAutoresizingMaskIntoConstraints = false
        lazyLogInButton.addTarget(self, action: #selector(loginAction), for: .touchUpInside)
        lazyLogInButton.heightAnchor.constraint(equalToConstant: 38).isActive = true
        return lazyLogInButton
    }()
    
    @objc
    private func loginAction() {
        validateURLAndLogin()
    }
    
    @objc
    private func goToGuide() {
        let userGuideViewController = UserGuideViewController()
        show(userGuideViewController, sender: nil)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        self.view.backgroundColor = .white
        self.setupView()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
    }
    
    private func setupView() {
        self.setupPresentation()
    }
    
    private func setupPresentation() {
        self.setupScrollView()
        self.setupContentView()
        self.setupTitleImageView()
        self.setupURLContainer()
        self.setupCredentalsLabel()
        self.setupLogInButton()
        self.setupGuideButton()
    }
    
    private func setupScrollView() {
        self.view.addSubview(self.scrollView)
        self.bindSubviewToEdges(self.scrollView)
    }
    
    private func setupContentView() {
        self.scrollView.addSubview(self.contentView)
        self.contentView.bindToEdges(ofView: self.scrollView)
        self.contentView.widthAnchor.constraint(equalTo: self.scrollView.widthAnchor).isActive = true
        self.contentView.heightAnchor.constraint(equalTo: self.scrollView.heightAnchor).isActive = true
    }
    
    private func setupTitleImageView() {
        self.contentView.addSubview(self.titleImageView)
        self.titleImageView.topAnchor.constraint(equalTo: self.contentView.topAnchor, constant: 21).isActive = true
        self.titleImageView.centerXAnchor.constraint(equalTo: self.contentView.centerXAnchor).isActive = true
    }
    
    private func setupURLContainer() {
        self.contentView.addSubview(self.urlContainer)
        
        self.urlContainer.centerYAnchor.constraint(equalTo: self.contentView.centerYAnchor).isActive = true
        self.urlContainer.leadingAnchor.constraint(equalTo: self.contentView.leadingAnchor, constant: 16).isActive = true
        self.urlContainer.trailingAnchor.constraint(equalTo: self.contentView.trailingAnchor, constant: -16).isActive = true
        
        let isValidUrlRule: ValidationRule = {
            let url = URL(string: $0)
            return url == nil ? Utils.localizeSelf("server_error_invalid_url_format") : nil
        }

        let urlMustContainHostRule: ValidationRule = {
            let url = URL(string: $0)!
            return url.host == nil ? Utils.localizeSelf("server_error_url_must_contain_host_component") : nil
        }

        self.urlContainer.setContainerLabel(Utils.localizeSelf("server_private_backend_address"))
        self.urlContainer.setValidationRules([isValidUrlRule, urlMustContainHostRule])
        self.setupURLContainerTextField()
    }
    
    private func setupCredentalsLabel() {
        self.contentView.addSubview(self.credentialsLabel)
        
        self.credentialsLabel.topAnchor.constraint(greaterThanOrEqualTo: self.titleImageView.bottomAnchor, constant: 8).isActive = true
        self.credentialsLabel.bottomAnchor.constraint(equalTo: self.urlContainer.topAnchor, constant: -36).isActive = true
        self.credentialsLabel.leadingAnchor.constraint(equalTo: self.contentView.leadingAnchor, constant: 16).isActive = true
        self.credentialsLabel.trailingAnchor.constraint(equalTo: self.contentView.trailingAnchor, constant: -16).isActive = true
    }
    
    private func setupLogInButton() {
        self.contentView.addSubview(self.logInButton)
        
        self.contentView.bottomAnchor.constraint(equalTo: self.logInButton.bottomAnchor, constant: 16.0).isActive = true
        self.logInButton.leadingAnchor.constraint(equalTo: self.contentView.leadingAnchor, constant: 16.0).isActive = true
        self.contentView.trailingAnchor.constraint(equalTo: self.logInButton.trailingAnchor, constant: 16.0).isActive = true
    }
    
    private func setupGuideButton() {
        self.contentView.addSubview(self.guideButton)
        
        self.logInButton.topAnchor.constraint(equalTo: self.guideButton.bottomAnchor, constant: 30.0).isActive = true
        self.guideButton.leadingAnchor.constraint(equalTo: self.contentView.leadingAnchor, constant: 16.0).isActive = true
        self.contentView.trailingAnchor.constraint(equalTo: self.guideButton.trailingAnchor, constant: 16.0).isActive = true
    }
}

extension ServerLoginViewController: UITextFieldDelegate {
    fileprivate func setupURLContainerTextField() {
        let textField = self.urlContainer.textField

        textField.delegate = self
        textField.keyboardType = .URL
        textField.returnKeyType = .go
        textField.text = Settings.baseURL
    }

    public func textFieldDidBeginEditing(_ textField: UITextField) {
        urlContainer.clearError()
    }

    public func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        validateURLAndLogin()
        return textField.resignFirstResponder()
    }

    private func validateURLAndLogin() {

        guard urlContainer.validate() else {
            return
        }

        Settings.baseURL = urlContainer.textField.text
        Settings.isBaseURLValidated = false

        self.isWaiting = true

        provider.promise(target: NetworkAPI.get_status)
                .done { [weak self] (result: StatusResponse) in
                    Settings.isBaseURLValidated = true

                    self?.isWaiting = false
                    self?.appDelegate.setMainRootViewController(animated: true)
                }
                .catch { [weak self] error in
                    self?.isWaiting = false
                    self?.requestFailed(error)
                }
    }
}
