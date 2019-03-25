import UIKit
import Common

class PriceLabel: UILabel {

    func updateState(to state: SwypeViewControllerState, price: Double) {
        
        print("[PriceLabel] switch to \(state)")
        
        switch state {
        case .readyToPurchaseSwypeCode:
            text = String(format: "%@ %.2f PF", localize("price_lbl"), price)

        case .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenPurchase,
             .purchasingSwypeCode,
             .recording,
             .finishingRecord,
             .submittingVideoHash,
             .confirmingVideoHashSubmission:
            text = ""
        }
    }
}
