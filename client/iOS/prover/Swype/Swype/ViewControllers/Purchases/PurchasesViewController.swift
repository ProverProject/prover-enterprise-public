import UIKit
import Common

class PurchasesViewController: BaseUserPagesViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()

        var dataSource: [SettingsCellType] = [.videoQuality, .showFps, .changeBackendUrl, .howToUse]

        if Settings.allowUserChooseSwypeType {
            dataSource.append(.useFastSwypeCode)
        }

        self.settingsViewController.setupDataSource(dataSource)
    }
    
    override func changeVideoQuality() {
        let videoQualityController = VideoQualityViewController()

        videoQualityController.delegate = self
        show(videoQualityController, sender: nil)
   }
}

extension PurchasesViewController: VideoQualityDelegate {
    func didUpdateResolution() {
        self.settingsViewController.didChangeResolution()
    }
}
