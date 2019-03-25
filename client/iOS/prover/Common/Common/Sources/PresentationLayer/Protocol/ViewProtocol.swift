import UIKit

extension UITableViewDelegate where Self: ViewInput {
    func setupTable(_ tableView: UITableView) { }
}

/// Common ViewInput protocol
public protocol ViewInput: class, Presentable {
    func setupInitialState()
    func setupAppearedState()
    func setupDissapearedState()
}

public extension ViewInput {
    func setupAppearedState() { }
    func setupDissapearedState() { }
}

/// Common ViewOutput protocol
public protocol ViewOutput: class {
    func viewIsReady()
    func viewWillAppear()
    func viewIsAppeared()
    func viewIsGone()
}

public extension ViewOutput {
    func viewWillAppear() { }
    func viewIsAppeared() { }
    func viewIsGone() { }
}
