import UIKit

class LineView: UIView {
    
    override func draw(_ rect: CGRect) {
        super.draw(rect)
        
        self.backgroundColor = CoreColors.lightGray.color
    }
    
    var isActive: Bool = false {
        didSet {
            changeLineColor()
        }
    }
    
    private func changeLineColor() {
        self.backgroundColor = isActive ? CoreColors.darkGray.color : CoreColors.lightGray.color
    }
}

class TextContainer: UIView {
    private lazy var stackView: UIStackView = {
        let lazyStackView = UIStackView(frame: self.frame)
        lazyStackView.axis = .vertical
        lazyStackView.translatesAutoresizingMaskIntoConstraints = false
        return lazyStackView
    }()
    
    private lazy var containerLabel: UILabel = {
        let textFieldLabel = UILabel()
        textFieldLabel.textColor = CoreColors.lightGray.color
        textFieldLabel.font = .systemFont(ofSize: 12.0)
        textFieldLabel.translatesAutoresizingMaskIntoConstraints = false
        return textFieldLabel
    }()
    
    private lazy var grayLine: LineView = {
        let lazyGrayLine = LineView()
        lazyGrayLine.backgroundColor = CoreColors.darkGray.color
        lazyGrayLine.translatesAutoresizingMaskIntoConstraints = false
        lazyGrayLine.heightAnchor.constraint(equalToConstant: 1).isActive = true
        return lazyGrayLine
    }()
    
    private lazy var errorLabel: UILabel = {
        let lazyErrorLabel = UILabel()
        lazyErrorLabel.textColor = .red
        lazyErrorLabel.font = .systemFont(ofSize: 12.0)
        lazyErrorLabel.translatesAutoresizingMaskIntoConstraints = false
        lazyErrorLabel.heightAnchor.constraint(equalToConstant: 15).isActive = true
        lazyErrorLabel.isHidden = true
        return lazyErrorLabel
    }()
    
    public private(set) lazy var textField: UITextField = {
        let lazyTextField = UITextField()
        lazyTextField.font = .systemFont(ofSize: 16.0)
        lazyTextField.textColor = CoreColors.darkGray.color
        lazyTextField.tintColor = CoreColors.darkGray.color
        lazyTextField.translatesAutoresizingMaskIntoConstraints = false
        lazyTextField.heightAnchor.constraint(equalToConstant: 36).isActive = true
        return lazyTextField
    }()
    
    private var validationRules: [ValidationRule] = []
    public var error: String? {
        didSet {
            self.errorLabel.isHidden = error == nil
            self.errorLabel.text = error
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.setupView()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        self.setupView()
    }
    
    private func setupView() {
        self.setupStackView()
    }
    
    private func setupStackView() {
        self.addSubview(self.stackView)
        self.stackView.bindToEdges(ofView: self)
        
        self.stackView.addArrangedSubview(self.containerLabel)
        self.stackView.addArrangedSubview(self.textField)
        self.stackView.addArrangedSubview(self.grayLine)
        self.stackView.addArrangedSubview(self.errorLabel)
    }
    
    public func setValidationRules(_ rules: [ValidationRule]) {
        self.validationRules = rules
    }
    
    public func setContainerLabel(_ text: String) {
        self.containerLabel.text = text
    }
    
    public func validate() -> Bool {
        standardizeURL()

        for rule in self.validationRules {
            if let error = rule(self.textField.text ?? "") {
                self.error = error
                return false
            }
        }

        self.error = nil
        return true
    }

    public func clearError() {
        self.error = nil
    }

    private func standardizeURL() {
        guard let text = self.textField.text, var url = URL(string: text)?.standardized else {
            return
        }
        
        // 'yahoo.com' in string such as 'yahoo.com:1234' reads as URL scheme, NOT as hostname!
        if url.scheme == nil || (url.scheme != nil && url.host == nil) {
            url = URL(string: "http://\(url.absoluteString)")!
        }

        if url.host != nil {
            var comps = URLComponents(url: url, resolvingAgainstBaseURL: false)!

            comps.path = ""
            url = comps.url!
        }

        self.textField.text = url.absoluteString
    }
}
