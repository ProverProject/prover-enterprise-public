import UIKit

class TabButton: UIButton {
    convenience init(with title: String) {
        self.init(type: .custom)
        
        let activeButtonColor = UIColor(white: 1.00, alpha: 1)
        let inactiveButtonColor = UIColor(white: 0.80, alpha: 1)
        
        self.setTitle(title.uppercased(), for: .normal)
        self.setTitleColor(inactiveButtonColor, for: .normal)
        self.setTitleColor(activeButtonColor, for: .selected)
        self.titleLabel?.font = UIFont.boldSystemFont(ofSize: 15)
    }
}

