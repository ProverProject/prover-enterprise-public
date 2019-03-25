import UIKit
import Common

protocol VideoQualityDelegate: class {
    func didUpdateResolution()
}

class UserPagesViewController: BaseUserPagesViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        var dataSource: [SettingsCellType] = [.videoQuality, .showFps, .changeBackendUrl, .howToUse]

        if Settings.allowUserChooseSwypeType {
            dataSource.append(.useFastSwypeCode)
        }

        if Settings.allowUserInvertSwypeDirectionsForFacingCamera {
            dataSource.append(.invertSwypeCodeDirections)
        }

        self.settingsViewController.setupDataSource(dataSource)
    }
    
    override func changeVideoQuality() {
        let videoQualityController = VideoQualityViewController()

        videoQualityController.delegate = self
        show(videoQualityController, sender: nil)
   }
}

extension UserPagesViewController: VideoQualityDelegate {
    func didUpdateResolution() {
        self.settingsViewController.didChangeResolution()
    }
}
