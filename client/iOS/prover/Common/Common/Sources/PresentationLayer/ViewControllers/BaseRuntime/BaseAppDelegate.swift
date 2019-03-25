import Foundation
import UIKit

open class BaseAppDelegate: UIResponder, UIApplicationDelegate {
    
    open var window: UIWindow?
    
    open func application(_ application: UIApplication, willFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        window = UIWindow(frame: UIScreen.main.bounds)
        
        if Settings.isBaseURLValidated {
            setMainRootViewController(animated: false)
        }
        else {
            setAuthRootViewController(animated: false)
        }
        
        return true
    }
    
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
            let options = UIWindow.TransitionOptions(direction: .toRight, style: .linear)
            window!.setRootViewController(navigationController, options: options)
        } else {
            window!.rootViewController = navigationController
            window!.makeKeyAndVisible()
        }
    }
}
