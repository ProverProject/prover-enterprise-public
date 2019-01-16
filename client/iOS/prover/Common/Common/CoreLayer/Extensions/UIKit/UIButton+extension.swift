import UIKit

public extension UIButton {
    /// Setup resized image for button frames
    ///
    /// - Parameters:
    ///   - image: UIImage object
    ///   - state: UIButton state
    func setResizedImage(_ image: UIImage?, for state: UIControl.State) {        
        self.setImage(image?.resizeImage(with: self.frame.size).withRenderingMode(.alwaysTemplate), for: state)
    }
    
}
