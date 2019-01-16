import UIKit

class TitleView: BaseNibView {
    
    @IBOutlet weak var balanceLabel: UILabel!
    @IBOutlet weak var countLabel: UILabel!
    
    override func commonSetup() {
        self.balanceLabel.text = "Balance"
    }
    
    public func setProofs(_ count: Double) {
        let proofsString = String(format: "%.2f PF", count)
        self.countLabel.text = proofsString
    }

}
