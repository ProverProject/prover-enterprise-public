import UIKit

open class DotsStackView: UIView {
    
    public var selectedColor: UIColor = .green
    public var normalColor: UIColor = .lightGray
    public var errorColor: UIColor = .red
    
    public var scale: CGFloat = 1.2
    
    private let stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.distribution = .equalCentering
        stackView.alignment = .center
        stackView.spacing = 8
        return stackView
    }()
    
    private var dotsCount: Int = 0 {
        willSet {
            for _ in 0..<newValue {
                self.stackView.addArrangedSubview(createSecureButton())
            }
        }
    }
    
    override public init(frame: CGRect) {
        super.init(frame: frame)
        
        self.commonSetup()
    }
    
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        self.commonSetup()
    }
    
    private func commonSetup() {
        self.setupLayout()
    }
    
    private func setupLayout() {
        addSubview(self.stackView)
        self.stackView.bindToEdges(ofView: self)
    }
    
    public func createDots(_ number: Int) {
        self.stackView.subviews.forEach({ $0.removeFromSuperview() })
        self.dotsCount = number
    }
    
    private func createSecureButton() -> UIButton {
        let button = SecureTextButton()
        button.scale = scale
        button.normalColor = self.normalColor
        button.selectedColor = self.selectedColor
        
        button.translatesAutoresizingMaskIntoConstraints = false
        button.heightAnchor.constraint(equalToConstant: 4).isActive = true
        button.widthAnchor.constraint(equalToConstant: 4).isActive = true
        return button
    }
    
    public func updateControls(_ totalCount: Int) {
        self.getAllButtons().enumerated().forEach({ (index, button) in
            if index <= (totalCount - 1) {
                if !button.isSelected { button.isSelected = true }
            } else {
                if button.isSelected { button.isSelected = false }
            }
            
        })
    }
    
    public func setError() {
        self.getAllButtons().forEach({ (button) in
            button.setError()
        })
    }
    
    public func reset() {
        self.getAllButtons().forEach({ (button) in
            button.reset()
        })
    }
    
    public func setLastStep() {
        self.updateControls(self.dotsCount)
    }
    
    private func getAllButtons() -> [SecureTextButton] {
        return self.stackView.arrangedSubviews as? [SecureTextButton] ?? []
    }
}
