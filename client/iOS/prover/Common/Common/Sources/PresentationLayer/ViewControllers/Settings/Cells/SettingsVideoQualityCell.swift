import UIKit

class SettingsVideoQualityCell: SettingsTableViewBaseCell {
    
    public var videoQualityLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15)
        label.textColor = .black
        label.setContentHuggingPriority(UILayoutPriority(rawValue: 900), for: .horizontal)
        label.setContentCompressionResistancePriority(UILayoutPriority(rawValue: 100), for: .horizontal)
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
