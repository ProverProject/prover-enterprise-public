import UIKit

open class SecureTextButton: UIButton {
    
    public var selectedColor: UIColor = .black
    public var normalColor: UIColor = .lightGray
    
    public var scale: CGFloat = 1.2
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.commonSetup()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        self.commonSetup()
    }
    
    private func commonSetup() {
        backgroundColor = self.normalColor
        layer.cornerRadius = 2
        
        layer.shadowPath = UIBezierPath(roundedRect: CGRect(x: 0, y: 0, width: 4, height: 4), cornerRadius: 2).cgPath
        layer.shadowColor = UIColor.lightGray.cgColor
        layer.shadowRadius = 1
        layer.shadowOpacity = 1
        layer.shadowOffset = CGSize.zero
    }
    
    override open var isSelected: Bool {
        didSet {
            self.transform = isSelected ? CGAffineTransform(scaleX: scale, y: scale) : CGAffineTransform(scaleX: 1, y: 1)
            self.backgroundColor = isSelected ? selectedColor.withAlphaComponent(0.6) : normalColor
        }
    }
    
    public func setError() {
        if !self.isSelected { self.isSelected = true }
        self.backgroundColor = UIColor.red.withAlphaComponent(0.6)
    }
    
    public func reset() {
        self.backgroundColor = normalColor
        if self.isSelected { self.isSelected = false }
    }
}
