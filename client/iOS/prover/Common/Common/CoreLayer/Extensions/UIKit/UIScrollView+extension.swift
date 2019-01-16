import UIKit

extension UIScrollView {
    /// Get current page of scroll view
    var currentPage: Int {
        return Int((self.contentOffset.x + (0.5*self.frame.size.width))/self.frame.width)
    }
}
