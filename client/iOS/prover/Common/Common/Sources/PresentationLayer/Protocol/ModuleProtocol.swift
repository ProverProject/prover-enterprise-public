import UIKit

public protocol ModuleInput: class {
    var viewController: UIViewController { get }
    
    func push(from viewController: UIViewController)
    func present(from viewController: UIViewController)
}

public extension ModuleInput {
    func push(from viewController: UIViewController) { }
    func present(from viewController: UIViewController) { }
}

public protocol ModuleOutput: class { }
