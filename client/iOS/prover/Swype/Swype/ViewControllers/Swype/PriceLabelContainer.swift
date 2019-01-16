import UIKit
import Common

class PriceLabelContainer: BasePriceLabelContainer {
    private let enoughFundsColor = UIColor.black.withAlphaComponent(0.25)
    private let notEnoughFundsColor = UIColor.red.withAlphaComponent(0.25)

    func update(by state: SwypeViewControllerState, isNotEnoughFunds: Bool) {
        switch state {
        case .readyToGetSwypeCode:
            backgroundColor = isNotEnoughFunds ? notEnoughFundsColor : enoughFundsColor
            isHidden = false

        case .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .waitingForCode,
             .didReceiveCode,
             .waitingForCircle,
             .waitingToStartSwypeCode,
             .detectingSwypeCode,
             .finishingWithoutDetectedSwypeCode,
             .swypeCodeDetected,
             .finishingWithDetectedSwypeCode,
             .submittingMediaHash,
             .confirmingMediaHashSubmission:
            backgroundColor = enoughFundsColor
            isHidden = true
        }
    }
}
