import UIKit
import Common

class UserPagesViewController: BaseUserPagesViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.settingsViewController.setupDataSource([.changeBackendUrl, .howToUse])
    }
}
