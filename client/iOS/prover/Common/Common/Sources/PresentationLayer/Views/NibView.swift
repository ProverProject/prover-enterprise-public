import UIKit

open class NibView: UIView {
    
    public var contentView: UIView!
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        // Setup view from .xib file
        xibSetup()
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        // Setup view from .xib file
        xibSetup()
    }
    
    open func commonSetup() { }
}

private extension NibView {
    
    func xibSetup() {
        contentView = loadNib()
        // use bounds not frame or it'll be offset
        contentView.frame = bounds
        // Adding custom subview on top of our view
        addSubview(contentView)
        
        contentView.translatesAutoresizingMaskIntoConstraints = false
        
        contentView.leadingAnchor.constraint(equalTo: self.leadingAnchor).isActive = true
        contentView.trailingAnchor.constraint(equalTo: self.trailingAnchor).isActive = true
        contentView.topAnchor.constraint(equalTo: self.topAnchor).isActive = true
        contentView.bottomAnchor.constraint(equalTo: self.bottomAnchor).isActive = true
        
        self.commonSetup()
    }
}
