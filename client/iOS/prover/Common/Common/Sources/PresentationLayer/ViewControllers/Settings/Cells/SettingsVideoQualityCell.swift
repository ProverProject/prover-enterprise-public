import UIKit

class SettingsVideoQualityCell: SettingsTableViewBaseCell {
    
    public var videoQualityLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15)
        label.textColor = .black
        return label
    }()
    
    public var videoQuality: String {
        get {
            return videoQualityLabel.text!
        }
        set {
            videoQualityLabel.text = newValue
        }
    }
    
    override func setupLayout() {
        super.setupLayout()
        
        stackView.addArrangedSubview(videoQualityLabel)
    }
}
