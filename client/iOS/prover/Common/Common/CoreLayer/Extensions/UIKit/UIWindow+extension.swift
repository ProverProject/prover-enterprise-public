import Foundation
import UIKit

public extension UIWindow {
    public func setMainRootViewController(animated: Bool) {
        let mainStoryboard = UIStoryboard(name: "Main", bundle: nil)
        let mainViewController = mainStoryboard.instantiateViewController(withIdentifier: "mainViewController")
        let navController = BaseNavigationController(rootViewController: mainViewController)
        
        setNavigationRootViewController(navController, animated: animated)
    }
        
    public func setAuthRootViewController(animated: Bool) {
        let navController = AuthNavigationController()
        
        setNavigationRootViewController(navController, animated: animated)
    }
    
    private func setNavigationRootViewController(_ navigationController: UINavigationController, animated: Bool) {
        
        navigationController.isNavigationBarHidden = true
        
        if animated {
            let options = TransitionOptions(direction: .toRight, style: .linear)
            setRootViewController(navigationController, options: options)
        } else {
            rootViewController = navigationController
            makeKeyAndVisible()
        }
    }
}
