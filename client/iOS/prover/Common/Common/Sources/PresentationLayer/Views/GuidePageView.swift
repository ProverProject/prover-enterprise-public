import UIKit

class GuidePageView: UIView {
    private let containerView: UIView = {
        let lazyContainerView = UIView()
        lazyContainerView.backgroundColor = .red
        lazyContainerView.translatesAutoresizingMaskIntoConstraints = false
        return lazyContainerView
    }()
    
    private lazy var imageView: UIImageView = {
        let lazyImageView = UIImageView()
        lazyImageView.contentMode = .center
        lazyImageView.translatesAutoresizingMaskIntoConstraints = false
        return lazyImageView
    }()
    
    private lazy var titleLabel: UILabel = {
        let lazyTitleLabel = UILabel()
        lazyTitleLabel.numberOfLines = 0
        lazyTitleLabel.lineBreakMode = .byWordWrapping
        lazyTitleLabel.textAlignment = .center
        lazyTitleLabel.translatesAutoresizingMaskIntoConstraints = false
        return lazyTitleLabel
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.commonSetup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        self.commonSetup()
    }
    
    private func commonSetup() {
        self.addSubview(containerView)
        self.addSubview(imageView)
        self.addSubview(titleLabel)
        
        self.setupLayout()
    }
    
    private func setupLayout() {
        NSLayoutConstraint.activate([
            containerView.centerXAnchor.constraint(equalTo: self.centerXAnchor),
            containerView.centerYAnchor.constraint(equalTo: self.centerYAnchor),
            
            imageView.topAnchor.constraint(equalTo: containerView.topAnchor),
            imageView.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            imageView.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            imageView.widthAnchor.constraint(equalToConstant: 200),
            imageView.heightAnchor.constraint(equalTo: imageView.widthAnchor, multiplier: 1.0),
            
            titleLabel.centerXAnchor.constraint(equalTo: containerView.centerXAnchor),
            titleLabel.widthAnchor.constraint(lessThanOrEqualToConstant: 220),
            titleLabel.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
            ])
    }
    
    public func setImage(_ image: UIImage?) {
        self.imageView.image = image
        self.setNeedsLayout()
        self.layoutIfNeeded()
    }
    
    public func setTitle(_ text: String) {
        self.titleLabel.text = text
        self.setNeedsLayout()
        self.layoutIfNeeded()
    }
}
