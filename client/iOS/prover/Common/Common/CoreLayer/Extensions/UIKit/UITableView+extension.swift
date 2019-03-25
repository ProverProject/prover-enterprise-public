import UIKit

public extension UITableView {
    func registerCell(nibName name: String) {
        self.register(UINib(nibName: name, bundle: nil), forCellReuseIdentifier: name)
    }
    
    func registerHeaderFooterView(nibName name: String) {
        self.register(UINib(nibName: name, bundle: nil), forHeaderFooterViewReuseIdentifier: name)
    }
    
    func registerCell(withCellClass cellClass: UITableViewCell.Type) {
        let cellClassName = String(describing: cellClass)
        self.register(cellClass, forCellReuseIdentifier: cellClassName)
    }
    
    func registerCellFromNib(withCellClass cellClass: UITableViewCell.Type) {
        let cellClassName = String(describing: cellClass)
        self.register(UINib(nibName: cellClassName, bundle: nil), forCellReuseIdentifier: cellClassName)
    }
    
    func registerHeaderFooterView(headerFooterView view: UITableViewHeaderFooterView.Type) {
        let className = String(describing: view)
        self.register(UINib(nibName: className, bundle: nil), forHeaderFooterViewReuseIdentifier: className)
    }
    
    
    
    func dequeReusableCell<T: UITableViewCell>(withCellClass cellClass: T.Type, for indexPath: IndexPath) -> T? {
        let cellClassName = String(describing: cellClass)
        
        return self.dequeueReusableCell(withIdentifier: cellClassName, for: indexPath) as? T
    }
}
