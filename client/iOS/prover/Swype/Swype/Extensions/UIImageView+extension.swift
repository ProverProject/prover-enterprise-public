import UIKit

extension UIImageView {
    
    func start() {
        setAnimatedVisibility(visible: true)
        startAnimating()
    }
    
    func stop() {
        stopAnimating()
        setAnimatedVisibility(visible: false)
    }
}
