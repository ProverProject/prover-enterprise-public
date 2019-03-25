import UIKit
import Common

class VideoQualityViewController: UITableViewController {
    private let availablePresets: [AVOutputSettingsPreset] = Settings.availableVideoOutputPresets()

    private var currentIndexPath = IndexPath()
    weak var delegate: VideoQualityDelegate?

    override func viewDidLoad() {
        super.viewDidLoad()

        customizeNavigationBar()

        tableView.rowHeight = 48
        tableView.tableFooterView = UIView()

        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "VideoQualityTableViewCell")
        
        currentIndexPath = IndexPath(row: availablePresets.index(of: Settings.selectedVideoOutputPreset)!,
                                     section: 0)
        
        navigationItem.title = "Video Quality"
    }
    
    private func customizeNavigationBar() {

        let bgImage = #imageLiteral(resourceName: "background").resizableImage(withCapInsets: UIEdgeInsets.zero,
                                        resizingMode: .stretch)
        
        navigationController?.navigationBar.setBackgroundImage(bgImage, for: .default)
        navigationController?.navigationBar.barStyle = .black
        
        let side: CGFloat = UIScreen.main.scale == 3 ? 36 : 30
        let backButton = UIButton(frame: CGRect(x: 0, y: 0, width: side, height: side))

        backButton.setImage(#imageLiteral(resourceName: "back").withRenderingMode(.alwaysOriginal), for: .normal)
        backButton.addTarget(self, action: #selector(backButtonAction), for: .touchUpInside)
        
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: backButton)
    }
    
    @objc private func backButtonAction(_ sender: UIButton) {
        navigationController?.popViewController(animated: true)
    }
}


// MARK: - UITableViewDelegate

extension VideoQualityViewController {
    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        // #warning Incomplete implementation, return the number of rows
        return availablePresets.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let preset = availablePresets[indexPath.row]
        let videoQuality = Settings.videoOutputResolution(forPreset: preset)
        let cell = tableView.dequeueReusableCell(withIdentifier: "VideoQualityTableViewCell", for: indexPath)

        if preset.rawValue.contains("HEVC") {
            cell.textLabel!.text = "\(videoQuality.width) × \(videoQuality.height) (HEVC)"
        }
        else {
            cell.textLabel!.text = "\(videoQuality.width) × \(videoQuality.height)"
        }
        
        if indexPath != currentIndexPath {
            cell.textLabel!.font = .forDeselectedItem
            cell.accessoryType = .none
        }
        else {
            cell.textLabel!.font = .forSelectedItem
            cell.accessoryType = .checkmark
        }
        
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath != currentIndexPath {
            let prevIndexPath = currentIndexPath
            currentIndexPath = indexPath

            let prevCell = tableView.cellForRow(at: prevIndexPath)
            let currentCell = tableView.cellForRow(at: currentIndexPath)

            prevCell?.textLabel!.font = .forDeselectedItem
            prevCell?.accessoryType = .none

            currentCell!.textLabel!.font = .forSelectedItem
            currentCell!.accessoryType = .checkmark

            Settings.selectedVideoOutputPreset = availablePresets[currentIndexPath.row]
            delegate?.didUpdateResolution()
        }
        
        navigationController?.popViewController(animated: true)
    }
}

fileprivate extension UIFont {
    static var forSelectedItem: UIFont { return UIFont.boldSystemFont(ofSize: 14) }
    static var forDeselectedItem: UIFont { return UIFont.systemFont(ofSize: 14) }
}
