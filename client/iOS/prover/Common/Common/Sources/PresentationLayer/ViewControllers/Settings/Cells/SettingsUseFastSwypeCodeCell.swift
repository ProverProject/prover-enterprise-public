import UIKit

class SettingsUseFastSwypeCodeCell: SettingsTableViewBaseCell {

    private lazy var switchControl: UISwitch = {
        let sw = UISwitch()
        sw.isOn = Settings.useFastSwypeCode
        sw.addTarget(self, action: #selector(switchAction), for: .valueChanged)
        return sw
    }()
    
    override func setupLayout() {
        super.setupLayout()
        
        stackView.addArrangedSubview(switchControl)
    }
    
    @objc private func switchAction(_ sender: UISwitch) {
        Settings.useFastSwypeCode = sender.isOn
    }
}
