import UIKit

open class UserGuideViewController: BaseViewController, UIScrollViewDelegate {
    
    private lazy var titleImageView: UIImageView = {
        let lazyTitleImageView = UIImageView(image: #imageLiteral(resourceName: "prover_title"))
        lazyTitleImageView.contentMode = .scaleAspectFit
        lazyTitleImageView.translatesAutoresizingMaskIntoConstraints = false
        return lazyTitleImageView
    }()
    private lazy var scrollView: UIScrollView = {
        let lazyScrollView = UIScrollView()
        lazyScrollView.delegate = self
        lazyScrollView.isPagingEnabled = true
        lazyScrollView.showsHorizontalScrollIndicator = false
        lazyScrollView.alwaysBounceHorizontal = false
        lazyScrollView.alwaysBounceVertical = false
        lazyScrollView.translatesAutoresizingMaskIntoConstraints = false
        return lazyScrollView
    }()
    private var pageControlContainer: UIView! = nil
    private var pageControl: UIPageControl! = nil
    
    private lazy var skipButton: UIButton = {
        let button = UIButton()
        button.setTitle("SKIP", for: .normal)
        button.setTitle("FINISH", for: .selected)
        button.setTitleColor(.black, for: .normal)
        button.addTarget(self, action: #selector(dismissButtonAction), for: .touchUpInside)
        button.translatesAutoresizingMaskIntoConstraints = false
        
        return button
    }()
    
    private var pages: [GuidePage] {
        return SharedSettings.shared.guidePages
    }
    
    override open func loadView() {
        super.loadView()
        
        setupLayout()
        
        setupPageControlContainer()
        setupScrollView()
        
        setupScrollViewContent()
    }
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        
        view.layoutMargins = UIEdgeInsets(top: 16, left: 16, bottom: 16, right: 16)
        view.backgroundColor = .white

    }
    
    open override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        navigationController!.setNavigationBarHidden(true, animated: true)
    }
    
    private func setupLayout() {
        self.view.addSubview(self.titleImageView)

        self.titleImageView.topAnchor.constraint(equalTo: self.layoutTopAnchor, constant: 21).isActive = true
        self.titleImageView.centerXAnchor.constraint(equalTo: self.view.centerXAnchor).isActive = true
    }

    private func setupPageControlContainer() {
        
        pageControlContainer = UIView()
        
        view.addSubview(pageControlContainer)
        
        pageControlContainer.translatesAutoresizingMaskIntoConstraints = false
        
        pageControlContainer.leadingAnchor.constraint(equalTo: view.layoutMarginsGuide.leadingAnchor).isActive = true
        pageControlContainer.trailingAnchor.constraint(equalTo: view.layoutMarginsGuide.trailingAnchor).isActive = true
        pageControlContainer.bottomAnchor.constraint(equalTo: view.layoutMarginsGuide.bottomAnchor).isActive = true
        
        pageControl = UIPageControl()
        
        pageControlContainer.addSubview(pageControl)
        
        pageControl.translatesAutoresizingMaskIntoConstraints = false
        pageControl.currentPageIndicatorTintColor = .black
        pageControl.pageIndicatorTintColor = UIColor(white: 0.90, alpha: 1)
        pageControl.numberOfPages = pages.count
        
        pageControl.centerXAnchor.constraint(equalTo: pageControlContainer.centerXAnchor).isActive = true
        pageControl.topAnchor.constraint(equalTo: pageControlContainer.topAnchor).isActive = true
        pageControl.bottomAnchor.constraint(equalTo: pageControlContainer.bottomAnchor).isActive = true
        
        pageControlContainer.addSubview(skipButton)
        
        skipButton.centerYAnchor.constraint(equalTo: pageControl.centerYAnchor).isActive = true
        skipButton.trailingAnchor.constraint(equalTo: pageControlContainer.trailingAnchor).isActive = true
    }
    
    @objc private func dismissButtonAction(_ sender: UIButton) {
        navigationController!.popViewController(animated: true)
    }
    
    private func setupScrollView() {
        view.addSubview(scrollView)

        scrollView.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        scrollView.topAnchor.constraint(equalTo: titleImageView.bottomAnchor).isActive = true
        scrollView.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        scrollView.bottomAnchor.constraint(equalTo: pageControlContainer.topAnchor).isActive = true
    }
    
    private func setupScrollViewContent() {
        let imageSizes = pages.map { $0.image.size }
        let maxWidth = imageSizes.map { $0.width }.max() ?? 0
        let maxHeight = imageSizes.map { $0.height }.max() ?? 0

        var prevPageTrailingAnchor = scrollView.leadingAnchor
        
        for page in pages {
            let pageView = UIView()
            let imageView = UIImageView(image: page.image)
            let titleLabel = UILabel()

            scrollView.addSubview(pageView)
            
            imageView.contentMode = .center
            
            pageView.translatesAutoresizingMaskIntoConstraints = false
            imageView.translatesAutoresizingMaskIntoConstraints = false
            titleLabel.translatesAutoresizingMaskIntoConstraints = false

            pageView.addSubview(imageView)
            pageView.addSubview(titleLabel)

            imageView.widthAnchor.constraint(equalToConstant: maxWidth).isActive = true
            imageView.heightAnchor.constraint(equalToConstant: maxHeight).isActive = true
            imageView.centerXAnchor.constraint(equalTo: pageView.centerXAnchor).isActive = true
            imageView.centerYAnchor.constraint(equalTo: pageView.centerYAnchor).isActive = true

            titleLabel.numberOfLines = 0
            titleLabel.lineBreakMode = .byWordWrapping
            titleLabel.text = page.title
            titleLabel.textAlignment = .center

            titleLabel.centerXAnchor.constraint(equalTo: pageView.centerXAnchor).isActive = true
            titleLabel.widthAnchor.constraint(equalToConstant: 250).isActive = true
            titleLabel.topAnchor.constraint(equalTo: imageView.bottomAnchor, constant: 24).isActive = true

            if page == pages.last! {
                pageView.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor).isActive = true
            }

            pageView.leadingAnchor.constraint(equalTo: prevPageTrailingAnchor).isActive = true
            pageView.topAnchor.constraint(equalTo: scrollView.topAnchor).isActive = true
            pageView.widthAnchor.constraint(equalTo: scrollView.widthAnchor).isActive = true
            pageView.heightAnchor.constraint(equalTo: scrollView.heightAnchor).isActive = true
            pageView.bottomAnchor.constraint(equalTo: scrollView.bottomAnchor).isActive = true

            prevPageTrailingAnchor = pageView.trailingAnchor

            if page == pages.last! {
                pageView.trailingAnchor.constraint(equalTo: scrollView.trailingAnchor).isActive = true
            }
        }
    }
    
    public func scrollViewDidScroll(_ scrollView: UIScrollView) {
        pageControl.currentPage = scrollView.currentPage
        skipButton.isSelected = pageControl.currentPage == (pages.count - 1) 
    }
}
