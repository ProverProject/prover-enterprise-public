import UIKit

enum BaseStoryboards: String {
    case License
    var instance: UIStoryboard {
        let baseBundle = Bundle(identifier: "ru.nordavind.Common")
        return UIStoryboard(name: self.rawValue, bundle: baseBundle)
    }
}

extension BaseStoryboards {
    func instantiateInitial<T>(_ viewController: T.Type) -> T? {
        return self.instance.instantiateInitialViewController() as? T
    }
    
    func instantiateViewController<T>(_ viewController: T.Type) -> T? {
        return self.instance.instantiateViewController(withIdentifier: String(describing: T.self)) as? T
    }
}
