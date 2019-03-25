import Foundation

enum SwypeViewControllerState {
    case requestingBalanceAndPriceOnAppearance
    case readyToPurchaseSwypeCode
    case requestingBalanceAndPriceThenPurchase
    case purchasingSwypeCode
    case recording
    case finishingRecord
    case submittingVideoHash
    case confirmingVideoHashSubmission
}
