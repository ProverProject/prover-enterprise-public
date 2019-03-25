import UIKit

public protocol Presentable {
    
    var viewController: UIViewController { get }
    
    func presentAsRoot()
    func present(fromViewController viewController: UIViewController)
    func presentModal(fromViewController viewController: UIViewController)
    func dissmiss()
    func dissmissModal(_ completion: @escaping () -> Void)
}

public extension Presentable {
    func dissmissModal(_ completion: @escaping () -> Void = { }) {
        dissmissModal(completion)
    }
}

public extension BaseAppDelegate {
    static var currentDelegate: BaseAppDelegate? {
        return UIApplication.shared.delegate as? BaseAppDelegate
    }
    
    static var currentWindow: UIWindow? {
        return currentDelegate?.window
    }
}

public extension Presentable where Self: UIViewController {
    
    var viewController: UIViewController {
        return self
    }
    
    func presentAsRoot() {
        BaseAppDelegate.currentWindow?.rootViewController = viewController
    }
    
    func present(fromViewController viewController: UIViewController) {
        viewController.navigationController?.pushViewController(self, animated: true)
    }
    
    func presentModal(fromViewController viewController: UIViewController) {
        viewController.present(self, animated: true, completion: nil)
    }
    
    func dissmiss() {
        _ = navigationController?.popViewController(animated: true)
    }
    
    func dissmissModal(_ completion: @escaping () -> Void = { }) {
        dismiss(animated: true, completion: completion)
    }
}
