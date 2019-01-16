import UIKit

class PriceLabel: UILabel {

    func updateState(by state: SwypeViewControllerState, price: Double) {
        
        print("[PriceLabel] switch to \(state)")
        
        switch state {
        case .readyToGetSwypeCode:
            text = String(format: "Price: %.2f PF", price)

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
            text = ""
        }
    }
}
