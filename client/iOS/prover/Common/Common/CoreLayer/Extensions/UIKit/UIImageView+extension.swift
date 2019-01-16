import UIKit

extension UIImageView {
    
    /// Initialize UIImageView for app logo
    ///
    /// - Parameter image: UIImage object. Default is Nil
    static func initForLogo(with image: UIImage? = nil) -> UIImageView {
        let logoImageView = UIImageView(image: image)
        logoImageView.contentMode = .scaleAspectFit
        logoImageView.translatesAutoresizingMaskIntoConstraints = false
        return logoImageView
    }
}
