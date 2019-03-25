import UIKit

public enum SettingsCellType {
    case videoQuality
    case showFps
    case changeBackendUrl
    case howToUse
    case useFastSwypeCode
    case invertSwypeCodeDirections
}

open class SettingsViewController: UIViewController {
    
    private lazy var tableView: UITableView = {
        return self.setupTableView()
    }()
    
    private var dataSource: [SettingsCellType] = [] {
        didSet {
            self.tableView.reloadData()
        }
    }
    
    open weak var delegate: SettingsViewControllerDelegate?
    
    override open func viewDidLoad() {
        super.viewDidLoad()
        
        self.commonSetup()
        // Do any additional setup after loading the view.
    }
    
    override open func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    public func setupDataSource(_ dataSource: [SettingsCellType]) {
        self.dataSource = dataSource
    }
    
    private func commonSetup() {
        self.configureLayout()
    }
    
    private func configureLayout() {
        self.view.addSubview(self.tableView)
        self.tableView.bindToEdges(ofView: self.view)
    }
    
    public func resetTableView() {
        self.tableView.reloadData()
    }
    
    public func didChangeResolution() {
        guard let index = self.dataSource.index(where: { $0 == .videoQuality }) else { return }
        let indexPath = IndexPath(row: index, section: 0)
        self.tableView.reloadRows(at: [indexPath], with: .fade)
    }
}

extension SettingsViewController: UITableViewDelegate, UITableViewDataSource {
    private func setupTableView() -> UITableView {
        let lazyTableView = UITableView(frame: self.view.frame, style: .plain)
        lazyTableView.backgroundColor = UIColor(white: 0.90, alpha: 1)
        
        lazyTableView.alwaysBounceVertical = false
        lazyTableView.alwaysBounceHorizontal = false
        lazyTableView.separatorInset = UIEdgeInsets(top: 0, left: 16, bottom: 0, right: 16)
        
        lazyTableView.translatesAutoresizingMaskIntoConstraints = false
        lazyTableView.rowHeight = UITableView.automaticDimension
        lazyTableView.estimatedRowHeight = 44
        lazyTableView.tableFooterView = UIView(frame: CGRect(x: 0, y: 0, width: self.view.frame.width, height: 1)) // For remove last separator =)
        
        lazyTableView.delegate = self
        lazyTableView.dataSource = self
        
        lazyTableView.registerCell(withCellClass: SettingsShowFpsCell.self)
        lazyTableView.registerCell(withCellClass: SettingsTableViewBaseCell.self)
        lazyTableView.registerCell(withCellClass: SettingsVideoQualityCell.self)
        
        if Settings.allowUserChooseSwypeType {
            lazyTableView.registerCell(withCellClass: SettingsUseFastSwypeCodeCell.self)
        }
        
        if Settings.allowUserInvertSwypeDirectionsForFacingCamera {
            lazyTableView.registerCell(withCellClass: SettingsInvertSwypeDirectionsCell.self)
        }
        
        return lazyTableView
    }
    
    public func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.dataSource.count
    }
    
    public func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellType = self.dataSource[indexPath.row]
        
        switch cellType {
        case .videoQuality:
            guard let cell = tableView.dequeReusableCell(withCellClass: SettingsVideoQualityCell.self, for: indexPath) else {
                return UITableViewCell()
            }
            let vQuality = Settings.selectedVideoOutputResolution
            
            cell.title = Utils.localizeSelf("settings_rec_resolution")
            cell.videoQuality = "\(vQuality.width)x\(vQuality.height)"
            
            return cell
        case .showFps:
            guard let cell = tableView.dequeReusableCell(withCellClass: SettingsShowFpsCell.self, for: indexPath) else {
                return UITableViewCell()
            }
            
            cell.title = Utils.localizeSelf("settings_show_fps")
            
            return cell
        case .changeBackendUrl:
            guard let cell = tableView.dequeReusableCell(withCellClass: SettingsTableViewBaseCell.self, for: indexPath) else {
                return UITableViewCell()
            }
            
            cell.title = Utils.localizeSelf("settings_change_server_address")
            
            return cell
        case .howToUse:
            guard let cell = tableView.dequeReusableCell(withCellClass: SettingsTableViewBaseCell.self, for: indexPath) else {
                return UITableViewCell()
            }
            
            cell.title = Utils.localizeSelf("settings_how_to")
            
            return cell
        case .useFastSwypeCode:
            guard let cell = tableView.dequeReusableCell(withCellClass: SettingsUseFastSwypeCodeCell.self, for: indexPath) else {
                return UITableViewCell()
            }
            
            cell.title = Utils.localizeSelf("settings_use_fast_swype")
            
            return cell
        case .invertSwypeCodeDirections:
            guard let cell = tableView.dequeReusableCell(withCellClass: SettingsInvertSwypeDirectionsCell.self, for: indexPath) else {
                return UITableViewCell()
            }
            
            cell.title = Utils.localizeSelf("settings_invert_swype")
            
            return cell
        }
    }
    
    public func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let cellType = self.dataSource[indexPath.row]
        self.delegate?.didSelect(cellType)
        
        tableView.deselectRow(at: indexPath, animated: true)
    }
}
