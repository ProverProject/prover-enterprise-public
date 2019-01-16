import UIKit

typealias ValidationRule = (String) -> String?

public class ValidatedTextField: UIStackView {
    
    private var errorLabel: UILabel!
    private var textField: UITextField!
    
    public var text: String? { return textField.text }
    
    public var isValid: Bool { return errorLabel?.text == nil }
    
    private var validationRules = [ValidationRule]()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required public init(coder: NSCoder) {
        super.init(coder: coder)
    }
    
    convenience init(text: String?, placeholder: String?, delegate: UITextFieldDelegate,
                     isSecure: Bool, keyboardType: UIKeyboardType,
                     validationRules: [ValidationRule]) {
        self.init()
        
        self.axis = .vertical
        self.distribution = .fill
        self.alignment = .fill
        
        self.validationRules = validationRules
        
        let content = createContent(
            text: text, placeholder: placeholder, delegate: delegate,
            isSecure: isSecure, keyboardType: keyboardType)
        
        for sv in content {
            addArrangedSubview(sv)
        }
    }
    
    private func createContent(text: String?, placeholder: String?, delegate: UITextFieldDelegate,
                               isSecure: Bool, keyboardType: UIKeyboardType) -> [UIView] {
        
        errorLabel = UILabel()
        
        errorLabel.translatesAutoresizingMaskIntoConstraints = false
        errorLabel.textColor = .red
        errorLabel.font = .systemFont(ofSize: 13)
        errorLabel.isHidden = true
        
        textField = UITextField()
        
        textField.translatesAutoresizingMaskIntoConstraints = false
        textField.text = text
        textField.placeholder = placeholder
        textField.isSecureTextEntry = isSecure
        textField.delegate = delegate
        textField.borderStyle = .none
        textField.keyboardType = keyboardType
        textField.font = .systemFont(ofSize: 15)
        
        return [errorLabel, textField]
    }
    
    public func clearText() {
        textField.text = nil
    }
    
    public func addTarget(_ target: Any?, action: Selector, for controlEvents: UIControl.Event) {
        textField.addTarget(target, action: action, for: controlEvents)
    }
    
    public func validate() {
        let error = validationRules.compactMap { $0(textField.text ?? "") }
            .first
        errorLabel.text = error
    }
    
    public func clearValidationError() {
        errorLabel.text = nil
    }
    
    public func showErrorIfInvalid() {
        errorLabel.isHidden = (errorLabel.text == nil)
    }
    
    public func hideError() {
        errorLabel.isHidden = true
    }
    
    public func setText(_ text: String?) {
        textField.text = text
    }
}
