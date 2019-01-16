import UIKit

public class AuthNavigationController: BaseNavigationController {
    
    public init() {
        super.init(nibName: nil, bundle: nil)
        
        isNavigationBarHidden = true
        
        let loginViewController = ServerLoginViewController()
        
        viewControllers.append(loginViewController)
        
        if Settings.isFirstLaunch {
            Settings.isFirstLaunch = false
            
            let userGuideViewController = UserGuideViewController()
            viewControllers.append(userGuideViewController)
        }
    }
    
    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
