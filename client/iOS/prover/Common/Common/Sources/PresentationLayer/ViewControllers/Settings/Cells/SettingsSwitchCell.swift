import UIKit

class SettingsSwitchCell: SettingsTableViewBaseCell {

    var switchInitValue: Bool {
        return false
    }

    private lazy var switchControl: UISwitch = {
        let sw = UISwitch()
        sw.setContentHuggingPriority(UILayoutPriority(rawValue: 900), for: .horizontal)
        sw.isOn = switchInitValue
        sw.addTarget(self, action: #selector(switchAction), for: .valueChanged)
        return sw
    }()

    override func setupLayout() {
        super.setupLayout()

        stackView.addArrangedSubview(switchControl)
    }

    @objc func switchAction(_ sender: UISwitch) { }
}
