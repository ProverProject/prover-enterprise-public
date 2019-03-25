import UIKit

class SettingsInvertSwypeDirectionsCell: SettingsSwitchCell {

    override var switchInitValue: Bool {
        return Settings.invertSwypeDirectionsForFacingCamera
    }

    override func switchAction(_ sender: UISwitch) {
        Settings.invertSwypeDirectionsForFacingCamera = sender.isOn
    }
}
