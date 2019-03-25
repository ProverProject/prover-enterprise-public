import UIKit

class SettingsUseFastSwypeCodeCell: SettingsSwitchCell {

    override var switchInitValue: Bool {
        return Settings.useFastSwypeCode
    }

    override func switchAction(_ sender: UISwitch) {
        Settings.useFastSwypeCode = sender.isOn
    }
}
