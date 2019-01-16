import UIKit
import Common

class RoundButton: NumericButton {
    
    private var _needShadow: Bool = false
    @IBInspectable var withShadow: Bool = false {
        willSet {
            self._needShadow = newValue
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        self.layer.cornerRadius = self.bounds.height / 2
        
        if _needShadow {
            self.setupShadow()
        }
    }
    
    private func setupShadow() {
        let sideSize: CGFloat = self.bounds.height
        
        self.layer.shadowPath = UIBezierPath(roundedRect: CGRect(x: 0, y: 0, width: sideSize, height: sideSize), cornerRadius: sideSize / 2).cgPath
        self.layer.shadowColor = UIColor.lightGray.cgColor
        self.layer.shadowRadius = 1
        self.layer.shadowOpacity = 1
        self.layer.shadowOffset = CGSize(width: 0, height: 1)
    }
}
