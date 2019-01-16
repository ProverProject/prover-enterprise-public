import UIKit

public extension UIViewController {
    /// Top ahchor of view controller
    public var layoutTopAnchor: NSLayoutYAxisAnchor {
        if #available(iOS 11.0, *) {
            return view.safeAreaLayoutGuide.topAnchor
        } else {
            return topLayoutGuide.bottomAnchor
        }
    }
    
    /// Bottom ahchor of view controller
    public var layoutBottomAnchor: NSLayoutYAxisAnchor {
        if #available(iOS 11.0, *) {
            return view.safeAreaLayoutGuide.bottomAnchor
        } else {
            return bottomLayoutGuide.topAnchor
        }
    }
    
    /// Binds subview constraints to UIViewController view sides
    ///
    /// - Parameter subview: UIView subview object
    public func bindSubviewToEdges(_ subview: UIView) {
        subview.translatesAutoresizingMaskIntoConstraints = false
        
        subview.topAnchor.constraint(equalTo: layoutTopAnchor).isActive = true
        subview.bottomAnchor.constraint(equalTo: layoutBottomAnchor).isActive = true
        subview.leadingAnchor.constraint(equalTo: self.view.leadingAnchor).isActive = true
        subview.trailingAnchor.constraint(equalTo: self.view.trailingAnchor).isActive = true
    }
} 
