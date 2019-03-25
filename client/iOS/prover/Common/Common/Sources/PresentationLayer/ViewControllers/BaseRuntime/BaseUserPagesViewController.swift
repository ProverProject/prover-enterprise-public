import UIKit
import StoreKit
import PromiseKit

public protocol SettingsViewControllerDelegate: class {
    func didSelect(_ cellType: SettingsCellType)
}

open class BaseUserPagesViewController: BaseViewController, SettingsViewControllerDelegate {
    
    private struct TabItem: Equatable {
        static func == (lhs: BaseUserPagesViewController.TabItem, rhs: BaseUserPagesViewController.TabItem) -> Bool {
            return lhs.tabButton == rhs.tabButton && lhs.tabController == rhs.tabController
        }
        
        var tabButton: TabButton
        let tabController: UIViewController
        
        init(withTitle title: String, andViewController viewController: UIViewController) {
            self.tabController = viewController
            self.tabButton = TabButton(with: title)
        }
    }
    
    private var navigationBackground: UIImageView!
    private var buttonsContainer: UIView!
    private var controllersContainer: UIView!
    private var whiteLine: UIView!
    
    public lazy var licenseViewController: LicenseViewController = {
        let lazyLicenseViewController = LicenseViewController()
        return lazyLicenseViewController
    }()
    
    public lazy var settingsViewController: SettingsViewController = {
        let lazySettingsViewController = SettingsViewController()
        lazySettingsViewController.delegate = self
        return lazySettingsViewController
    }()
    
    private lazy var tabsDataSource: [TabItem] = {
        return configureDataSource()
    }()
    
    private var scrollView: UIScrollView!
    
    open var isFirstlyAppeared = false
    
    open var settingsDataSource: [SettingsCellType]!
    
    open var openSettings: Bool = false
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        self.view.backgroundColor = .white
        
        configureButtonContainer()
        configureNavigationBackground()
        configureControllersContainer()
        configureScrollView()
        
        configureTabItems()
        
        navigationItem.prompt = Utils.localizeSelf("balance_title")
        navigationItem.title = String(format: Utils.localizeSelf("balance_str"), Double(0))
    }
    
    override open func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        setupNavigationBarAppearance()
        setupNavigationItemImagesAndActions()
    }
    
    override open func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        if openSettings {
            goToSettings()
        }
        
        if !isFirstlyAppeared {
            refreshBalance()
            isFirstlyAppeared = true
        }
    }
    
    private func configureDataSource() -> [TabItem] {
        let leftItem = TabItem(withTitle: Utils.localizeSelf("balance_license"), andViewController: licenseViewController)
        let rightItem = TabItem(withTitle: Utils.localizeSelf("balance_settings"), andViewController: settingsViewController)
        
        return [leftItem, rightItem]
    }
    
    @objc private func changeController(_ sender: UIButton) {
        sender.isSelected = !sender.isSelected
        
        if let index = tabsDataSource.index(where: { $0.tabButton == sender }) {
            tabsDataSource.filter({ $0.tabButton != sender }).forEach({ $0.tabButton.isSelected = false })
            
            let xPoint = scrollView.frame.width*CGFloat(index)
            scrollView.setContentOffset(CGPoint(x: xPoint, y: 0), animated: true)
        }
    }
    
    // MARK: - NavigationBar
    
    private func setupNavigationBarAppearance() {
        navigationController?.setNavigationBarHidden(false, animated: true)
        
        if let navigationBar = navigationController?.navigationBar {
            // Make navigation bar transparent
            navigationBar.setBackgroundImage(UIImage(), for: .default)
            navigationBar.shadowImage = UIImage()
            navigationBar.isTranslucent = true
            
            // Make the white title
            navigationBar.barStyle = .black
            
            // Enlarge title
            navigationBar.titleTextAttributes =
                [.font: UIFont.boldSystemFont(ofSize: 21)]
        }
    }
    
    private func setupNavigationItemImagesAndActions() {
        let side: CGFloat = UIScreen.main.scale == 3 ? 36 : 30
        
        let backButton = UIButton(frame: CGRect(x: 0, y: 0, width: side, height: side))
        let refreshButton = UIButton(frame: CGRect(x: 0, y: 0, width: side, height: side))
        
        backButton.setImage(#imageLiteral(resourceName: "back"), for: .normal)
        backButton.addTarget(self, action: #selector(backButtonAction), for: .touchUpInside)
        
        refreshButton.setImage(#imageLiteral(resourceName: "refresh"), for: .normal)
        refreshButton.addTarget(self, action: #selector(refreshBalanceAction), for: .touchUpInside)
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: backButton)
        navigationItem.rightBarButtonItem = UIBarButtonItem(customView: refreshButton)
    }
    
    @objc private func backButtonAction(_ sender: UIBarButtonItem) {
        navigationController?.popViewController(animated: true)
    }
    
    @objc private func refreshBalanceAction(_ sender: UIButton) {
        let entry = NetworkAPI.get_balance
        
        sender.startRotating()
        
        provider.promise(target: entry)
            .done { [weak self] (result: BalanceResponse) in
                let balanceItem = result.balance.first(where: {
                    $0.mosaicId.namespaceId == .prover
                })

                let balance = balanceItem?.floatQuantity ?? 0
                self?.navigationItem.title = String(format: Utils.localizeSelf("balance_str"), balance)
            }.ensure { [weak self] in
                if self != nil {
                    sender.stopRotating()
                }
            }.catch { [weak self] error in
                self?.requestFailed(error.localizedDescription)
        }
    }
    
    public func refreshBalance() {
        refreshBalanceAction(navigationItem.rightBarButtonItem!.customView as! UIButton)
    }
    
    // MARK: - SettingsViewController selected methods
    private func changeBackendUrl() {
        appDelegate.setAuthRootViewController(animated: true)
    }
    
    private func showUserGuide() {
        let userGuideViewController = UserGuideViewController()
        show(userGuideViewController, sender: nil)
    }
    
    open func changeVideoQuality() { }
    
    open func goToSettings() {
        guard let settingsTab = tabsDataSource.first(where: { $0.tabController is SettingsViewController }) else { return }
        changeController(settingsTab.tabButton)
    }
    
    public func didSelect(_ cellType: SettingsCellType) {
        switch cellType {
        case .videoQuality:
            changeVideoQuality()
        case .changeBackendUrl:
            changeBackendUrl()
        case .howToUse:
            showUserGuide()
        case .showFps,
             .useFastSwypeCode,
             .invertSwypeCodeDirections:
            break
        }
    }
}

// MARK: - Configure layout

extension BaseUserPagesViewController {
    private func configureButtonContainer() {
        buttonsContainer = UIView()
        view.addSubview(buttonsContainer)
        
        buttonsContainer.translatesAutoresizingMaskIntoConstraints = false
        buttonsContainer.topAnchor.constraint(equalTo: topAnchor).isActive = true
        buttonsContainer.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        buttonsContainer.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        buttonsContainer.heightAnchor.constraint(equalToConstant: 50).isActive = true
    }
    
    private func configureNavigationBackground() {
        navigationBackground = UIImageView(image: #imageLiteral(resourceName: "background"))
        view.addSubview(navigationBackground)
        
        navigationBackground.translatesAutoresizingMaskIntoConstraints = false
        
        navigationBackground.topAnchor.constraint(equalTo: view.topAnchor).isActive = true
        navigationBackground.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        navigationBackground.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        navigationBackground.bottomAnchor.constraint(equalTo: buttonsContainer.bottomAnchor).isActive = true
        
        view.sendSubviewToBack(navigationBackground)
    }
    
    private func configureControllersContainer() {
        controllersContainer = UIView()
        view.addSubview(controllersContainer)
        
        controllersContainer.translatesAutoresizingMaskIntoConstraints = false
        
        controllersContainer.topAnchor.constraint(equalTo: buttonsContainer.bottomAnchor).isActive = true
        controllersContainer.leadingAnchor.constraint(equalTo: view.leadingAnchor).isActive = true
        controllersContainer.trailingAnchor.constraint(equalTo: view.trailingAnchor).isActive = true
        controllersContainer.bottomAnchor.constraint(equalTo: view.bottomAnchor).isActive = true
    }
    
    private func configureScrollView() {
        scrollView = UIScrollView()
        scrollView.delegate = self
        scrollView.isPagingEnabled = true
        
        scrollView.backgroundColor = UIColor(white: 0.9, alpha: 1)
        scrollView.bounces = false
        scrollView.alwaysBounceHorizontal = false
        scrollView.showsHorizontalScrollIndicator = false
        
        controllersContainer.addSubview(scrollView)
        
        scrollView.translatesAutoresizingMaskIntoConstraints = false
        scrollView.topAnchor.constraint(equalTo: controllersContainer.topAnchor).isActive = true
        scrollView.leadingAnchor.constraint(equalTo: controllersContainer.leadingAnchor).isActive = true
        scrollView.trailingAnchor.constraint(equalTo: controllersContainer.trailingAnchor).isActive = true
        scrollView.bottomAnchor.constraint(equalTo: controllersContainer.bottomAnchor).isActive = true
        
        scrollView.contentSize = CGSize(width: view.frame.width * 2, height: scrollView.frame.height)
    }
    
    private func configureTabItems() {
        var buttons: [UIButton] = []
        
        tabsDataSource.enumerated().forEach { (index, tabItem) in
            addChild(tabItem.tabController)
            
            tabItem.tabController.didMove(toParent: self)
            scrollView.addSubview(tabItem.tabController.view)
            
            let frame = CGRect(x: view.frame.width*CGFloat(index), y: 0, width: scrollView.frame.width, height: scrollView.frame.height)
            tabItem.tabController.view.frame = frame
            
            tabItem.tabButton.isSelected = index == 0
            tabItem.tabButton.addTarget(self, action: #selector(changeController), for: .touchUpInside)
            
            buttons.append(tabItem.tabButton)
        }
        
        let stackView = UIStackView(arrangedSubviews: buttons)
        stackView.alignment = .center
        stackView.distribution = .fillEqually
        stackView.axis = .horizontal
        
        buttonsContainer.addSubview(stackView)
        
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.topAnchor.constraint(equalTo: buttonsContainer.topAnchor).isActive = true
        stackView.leadingAnchor.constraint(equalTo: buttonsContainer.leadingAnchor).isActive = true
        stackView.trailingAnchor.constraint(equalTo: buttonsContainer.trailingAnchor).isActive = true
        stackView.bottomAnchor.constraint(equalTo: buttonsContainer.bottomAnchor).isActive = true
        
        whiteLine = UIView(frame: CGRect(x: 0, y: 47, width: view.frame.width/CGFloat(tabsDataSource.count), height: 3))
        whiteLine.backgroundColor = .white
        
        buttonsContainer.addSubview(whiteLine)
    }
}

// MARK: - UIScrollViewDelegate

extension BaseUserPagesViewController: UIScrollViewDelegate {
    public func scrollViewDidScroll(_ scrollView: UIScrollView) {
        whiteLine.frame.origin.x = scrollView.contentOffset.x/2
    }
    
    public func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        let currentButton = tabsDataSource[scrollView.currentPage].tabButton
        tabsDataSource.filter({ $0.tabButton != currentButton }).forEach({ $0.tabButton.isSelected = false })
        currentButton.isSelected = true
    }
}
