import UIKit

extension UIStoryboard {
    
    /// Generic function for initialising view controller from storyboard
    ///
    /// - Parameters:
    ///   - storyboardName: String
    ///   - controllerName: String
    class func initViewController<T: UIViewController>(fromStoryboard storyboardName: String, withName controllerName: String) -> T? {
        return self.init(name: storyboardName, bundle: nil).instantiateViewController(withIdentifier: controllerName) as? T
    }
}
