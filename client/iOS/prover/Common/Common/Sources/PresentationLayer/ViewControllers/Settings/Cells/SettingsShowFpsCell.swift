import UIKit

class SettingsShowFpsCell: SettingsTableViewBaseCell {

    private lazy var switchControl: UISwitch = {
        let sw = UISwitch()
        sw.isOn = Settings.showFps
        sw.addTarget(self, action: #selector(switchAction), for: .valueChanged)
        return sw
    }()
    
    override func setupLayout() {
        super.setupLayout()
        
        stackView.addArrangedSubview(switchControl)
    }
    
    @objc private func switchAction(_ sender: UISwitch) {
        Settings.showFps = sender.isOn
    }
}
