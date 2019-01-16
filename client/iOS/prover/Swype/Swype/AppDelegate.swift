import Foundation
import UIKit
import Common

@UIApplicationMain
public class AppDelegate: BaseAppDelegate {

    override public func application(_ application: UIApplication, willFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        SharedSettings.shared.iosApp = "swypeid"
        
        SharedSettings.shared.guidePages = [
            GuidePage(image: #imageLiteral(resourceName: "guide_1"), title: "Login to private backend"),
            GuidePage(image: #imageLiteral(resourceName: "guide_2"), title: "Recharge your balance if needed"),
            GuidePage(image: #imageLiteral(resourceName: "guide_3"), title: "Set up the required video quality"),
            GuidePage(image: #imageLiteral(resourceName: "guide_4"), title: "Start filming"),
            GuidePage(image: #imageLiteral(resourceName: "guide_5"), title: "To activate the SWYPE ID function, make a circular movement"),
            GuidePage(image: #imageLiteral(resourceName: "guide_6"), title: "Enter SWYPE code, moving the phone along the arrows to combine the circles"),
            GuidePage(image: #imageLiteral(resourceName: "guide_7"), title: "Continue filming"),
            GuidePage(image: #imageLiteral(resourceName: "guide_8"), title: "Finish filming and keep the file"),
            GuidePage(image: #imageLiteral(resourceName: "guide_9"), title: "For authentication, upload the file to the _product.prover.io_ validation service")
        ]

        SharedSettings.shared.shouldAskForAccessToCamera = true
        SharedSettings.shared.shouldAskForAccessToMicrophone = true
        SharedSettings.shared.shouldAskForAccessToPhotosGallery = true
        
        return super.application(application, willFinishLaunchingWithOptions: launchOptions)
    }
}
