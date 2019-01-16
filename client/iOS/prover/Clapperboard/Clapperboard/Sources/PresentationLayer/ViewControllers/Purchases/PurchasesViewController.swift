import UIKit
import Common

class PurchasesViewController: BaseUserPagesViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.settingsViewController.setupDataSource([.changeBackendUrl, .howToUse])
    }
}
