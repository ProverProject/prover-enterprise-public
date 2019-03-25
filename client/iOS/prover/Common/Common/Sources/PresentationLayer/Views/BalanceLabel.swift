import Foundation
import UIKit

public class BalanceLabel: UILabel {
    public func update(balance: Double) {
        text = String(format: Utils.localizeSelf("balance_str"), balance)
    }
}
