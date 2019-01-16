import UIKit

extension UIView {
    private var titleImageViewTag: Int { return 4001 }
    private var infoLabelTag: Int { return 4002 }
    private var textInputViewsTag: Int { return 4003 }
    
    
    @discardableResult
    public func addTitleImageView(with image: UIImage? = nil) -> UIImageView {
        let titleImageView: UIImageView = .initForLogo(with: image)
        
        addSubview(titleImageView)
        
        titleImageView.translatesAutoresizingMaskIntoConstraints = false
        titleImageView.tag = titleImageViewTag
        
        titleImageView.topAnchor.constraint(equalTo: self.topAnchor, constant: 21).isActive = true
        titleImageView.leadingAnchor.constraint(equalTo: self.leadingAnchor).isActive = true
        titleImageView.trailingAnchor.constraint(equalTo: self.trailingAnchor).isActive = true
        titleImageView.heightAnchor.constraint(equalTo: self.widthAnchor, multiplier: 48/320).isActive = true
        
        return titleImageView
    }
    
    public func addInfoLabel(text: String?) -> UILabel {
        let infoLabel: UILabel = UILabel()
        
        addSubview(infoLabel)
        
        infoLabel.translatesAutoresizingMaskIntoConstraints = false
        infoLabel.text = text
        infoLabel.textAlignment = .center
        infoLabel.numberOfLines = 0
        infoLabel.font = .systemFont(ofSize: 15)
        infoLabel.textColor = UIColor(white: 0.70, alpha: 1)
        
        infoLabel.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        infoLabel.widthAnchor.constraint(equalToConstant: 100).isActive = true
        
        return infoLabel
    }
    
    public func addTextInputViews(delegate: UITextFieldDelegate, aboveBottomAnchor: NSLayoutYAxisAnchor) -> (stack: UIStackView, login: ValidatedTextField, password: ValidatedTextField) {
        
        let isLoginNotEmptyRule: ValidationRule = {
            let isEmpty = $0.isEmpty
            return !isEmpty ? nil : "Please, enter a login name"
        }
        
        let isValidEmailRule: ValidationRule = {
            let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
            let emailTest = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
            let isValid =  emailTest.evaluate(with: $0)
            
            return isValid ? nil : "Login name must be a valid e-mail address"
        }
        
        let isPasswordNotEmptyRule: ValidationRule = {
            let isEmpty = $0.isEmpty
            return !isEmpty ? nil : "Please, enter a password"
        }
        
        let isPasswordLongEnoughRule: ValidationRule = {
            let minPasswordLength = 6
            let isValid = $0.count >= minPasswordLength
            
            return isValid ? nil : "Password must be at least \(minPasswordLength) characters long"
        }
        
        let loginContainer = textInputContainer(
            text: nil, placeholder: "Login (E-mail)", delegate: delegate,
            isSecure: false, keyboardType: .emailAddress, validationRules: [isLoginNotEmptyRule, isValidEmailRule])
        
        let passwordContainer = textInputContainer(
            text: nil, placeholder: "Password", delegate: delegate,
            isSecure: true, keyboardType: .default, validationRules: [isPasswordNotEmptyRule, isPasswordLongEnoughRule])
        
        let stack = UIStackView(arrangedSubviews: [loginContainer.container, passwordContainer.container])
        
        addSubview(stack)
        
        stack.tag = textInputViewsTag
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.axis = .vertical
        stack.distribution = .fillEqually
        stack.alignment = .fill
        
        stack.topAnchor.constraint(equalTo: aboveBottomAnchor, constant: 30).isActive = true
        stack.leadingAnchor.constraint(equalTo: layoutMarginsGuide.leadingAnchor).isActive = true
        stack.trailingAnchor.constraint(equalTo: layoutMarginsGuide.trailingAnchor).isActive = true
        stack.centerYAnchor.constraint(equalTo: centerYAnchor).isActive = true
        
        return (stack, loginContainer.validatedTextField, passwordContainer.validatedTextField)
    }
    
    public func addPerformButton(text: String?, aboveBottomAnchor: NSLayoutYAxisAnchor) -> UIButton {
        let button = CustomButton()
        
        addSubview(button)
        
        button.translatesAutoresizingMaskIntoConstraints = false
        button.setTitle(text, for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel!.font = .systemFont(ofSize: 15)
        
        button.topAnchor.constraint(equalTo: aboveBottomAnchor, constant: 38).isActive = true
        button.leadingAnchor.constraint(equalTo: layoutMarginsGuide.leadingAnchor).isActive = true
        button.trailingAnchor.constraint(equalTo: layoutMarginsGuide.trailingAnchor).isActive = true
        button.heightAnchor.constraint(equalToConstant: 38).isActive = true
        
        return button
    }
    
    public func addForgotPasswordButton(text: String?, aboveBottomAnchor: NSLayoutYAxisAnchor) -> UIButton {
        let button = UIButton()
        
        addSubview(button)
        
        button.translatesAutoresizingMaskIntoConstraints = false
        button.setTitle(text, for: .normal)
        button.setTitleColor(UIColor(white: 0.70, alpha: 1), for: .normal)
        button.titleLabel!.font = .systemFont(ofSize: 15)
        button.isEnabled = true
        
        button.topAnchor.constraint(equalTo: aboveBottomAnchor, constant: 16).isActive = true
        button.centerXAnchor.constraint(equalTo: centerXAnchor).isActive = true
        
        return button
    }
    
    public func addSwitchCredentialsControllerButton(text: String?) -> UIButton {
        let button = CustomButton()
        
        addSubview(button)
        
        button.translatesAutoresizingMaskIntoConstraints = false
        button.setTitle(text, for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel!.font = .systemFont(ofSize: 15)
        button.isEnabled = true
        
        button.leadingAnchor.constraint(equalTo: layoutMarginsGuide.leadingAnchor).isActive = true
        button.trailingAnchor.constraint(equalTo: layoutMarginsGuide.trailingAnchor).isActive = true
        button.bottomAnchor.constraint(equalTo: layoutMarginsGuide.bottomAnchor).isActive = true
        button.heightAnchor.constraint(equalToConstant: 38).isActive = true
        
        return button
    }
    
    private func textInputContainer(text: String?, placeholder: String?, delegate: UITextFieldDelegate,
                                    isSecure: Bool, keyboardType: UIKeyboardType,
                                    validationRules: [ValidationRule]) -> (container: UIView, validatedTextField: ValidatedTextField) {
        let container = UIView()
        let validatedTextField = ValidatedTextField(
            text: text, placeholder: placeholder, delegate: delegate,
            isSecure: isSecure, keyboardType: keyboardType, validationRules: validationRules)
        let bottomBar = UIView()
        
        container.addSubview(validatedTextField)
        container.addSubview(bottomBar)
        
        container.translatesAutoresizingMaskIntoConstraints = false
        
        validatedTextField.translatesAutoresizingMaskIntoConstraints = false
        validatedTextField.leadingAnchor.constraint(equalTo: container.leadingAnchor).isActive = true
        validatedTextField.trailingAnchor.constraint(equalTo: container.trailingAnchor).isActive = true
        
        bottomBar.translatesAutoresizingMaskIntoConstraints = false
        bottomBar.backgroundColor = UIColor(white: 155.0/255, alpha: 1)
        bottomBar.heightAnchor.constraint(equalToConstant: 1).isActive = true
        bottomBar.leadingAnchor.constraint(equalTo: container.leadingAnchor).isActive = true
        bottomBar.trailingAnchor.constraint(equalTo: container.trailingAnchor).isActive = true
        bottomBar.topAnchor.constraint(equalTo: validatedTextField.bottomAnchor, constant: 8).isActive = true
        bottomBar.bottomAnchor.constraint(equalTo: container.bottomAnchor).isActive = true
        
        container.heightAnchor.constraint(equalToConstant: 51).isActive = true
        
        return (container, validatedTextField)
    }
    
    private class CustomButton: UIButton {
        override open var isEnabled: Bool {
            didSet {
                backgroundColor = isEnabled
                    ? UIColor(red: 59.0/255, green: 61.0/255, blue: 71.0/255, alpha: 1)
                    : UIColor(white: 204.0/255, alpha: 1)
            }
        }
    }
}
