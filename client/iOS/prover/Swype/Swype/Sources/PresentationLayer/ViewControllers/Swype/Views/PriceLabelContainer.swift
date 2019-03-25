import UIKit
import Common

class PriceLabelContainer: BasePriceLabelContainer {
    private let enoughFundsColor = UIColor.black.withAlphaComponent(0.45)
    private let notEnoughFundsColor = UIColor.red.withAlphaComponent(0.25)

    func update(to state: SwypeViewControllerState, isNotEnoughFunds: Bool) {
        switch state {
        case .readyToPurchaseSwypeCode:
            backgroundColor = isNotEnoughFunds ? notEnoughFundsColor : enoughFundsColor
            isHidden = false

        case .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenPurchase,
             .purchasingSwypeCode,
             .recording,
             .finishingRecord,
             .submittingVideoHash,
             .confirmingVideoHashSubmission:
            backgroundColor = enoughFundsColor
            isHidden = true
        }
    }
}
