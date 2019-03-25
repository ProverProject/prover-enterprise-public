import UIKit

open class SettingsTableViewBaseCell: UITableViewCell {

    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.numberOfLines = 0
        label.lineBreakMode = .byWordWrapping
        label.font = .systemFont(ofSize: 15)
        label.textColor = .black
        return label
    }()
    
    public lazy var stackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.alignment = .center
        stackView.distribution = .fill
        stackView.spacing = 12
        
        return stackView
    }()

    public var title: String {
        get {
            return titleLabel.text!
        }
        set {
            titleLabel.text = newValue
        }
    }

    override public init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)

        setupLayout()
    }
    
    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupLayout() {

        contentView.addSubview(stackView)
        
        let margins = contentView.layoutMargins

        contentView.layoutMargins = UIEdgeInsets(top: 16, left: margins.left, bottom: 16, right: margins.right)

        let guide = contentView.layoutMarginsGuide

        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.leadingAnchor.constraint(equalTo: guide.leadingAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: guide.trailingAnchor).isActive = true
        stackView.topAnchor.constraint(equalTo: guide.topAnchor).isActive = true
        stackView.bottomAnchor.constraint(equalTo: guide.bottomAnchor).isActive = true
        
        stackView.addArrangedSubview(titleLabel)
    }
}
