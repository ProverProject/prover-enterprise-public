import UIKit

class SettingsShowFpsCell: SettingsSwitchCell {

    override var switchInitValue: Bool {
        return Settings.showFps
    }

    override func switchAction(_ sender: UISwitch) {
        Settings.showFps = sender.isOn
    }
}
