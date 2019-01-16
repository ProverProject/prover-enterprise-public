import UIKit

/// Enumeration for base app colors
enum CoreColors {
    case lightGray, darkGray
    
    var color: UIColor {
        switch self {
        case .lightGray:
            return UIColor(hexString: "9B9B9B")
        case .darkGray:
            return UIColor(hexString: "3B3D47")
        }
    }
}
