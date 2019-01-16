import Foundation
import UIKit

open class BaseAppDelegate: UIResponder, UIApplicationDelegate {
    
    open var window: UIWindow?
    
    open func application(_ application: UIApplication, willFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        window = UIWindow(frame: UIScreen.main.bounds)

        if Settings.isBaseURLValidated {
            window!.setMainRootViewController(animated: false)
        }
        else {
            window!.setAuthRootViewController(animated: false)
        }
        
        return true
    }
}
