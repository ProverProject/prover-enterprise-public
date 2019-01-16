import UIKit

extension UIView {
    private var waiterTag: Int { return 5001 }

    func addWaiterView() {
        guard viewWithTag(waiterTag) == nil else {
            return
        }

        let containerView = UIView()
        let waiter = UIActivityIndicatorView()

        containerView.tag = waiterTag
        containerView.translatesAutoresizingMaskIntoConstraints = false
        containerView.isUserInteractionEnabled = true
        containerView.backgroundColor = UIColor(white: 0, alpha: 0.20)

        waiter.translatesAutoresizingMaskIntoConstraints = false
        //waiter.activityIndicatorViewStyle = .whiteLarge
        waiter.startAnimating()

        containerView.addSubview(waiter)
        addSubview(containerView)

        waiter.centerXAnchor.constraint(equalTo: containerView.centerXAnchor).isActive = true
        waiter.centerYAnchor.constraint(equalTo: containerView.centerYAnchor).isActive = true

        containerView.topAnchor.constraint(equalTo: topAnchor).isActive = true
        containerView.leadingAnchor.constraint(equalTo: leadingAnchor).isActive = true
        containerView.trailingAnchor.constraint(equalTo: trailingAnchor).isActive = true
        containerView.bottomAnchor.constraint(equalTo: bottomAnchor).isActive = true
    }

    func removeWaiterView() {
        viewWithTag(waiterTag)?.removeFromSuperview()
    }
}
