import Foundation

enum SwypeViewControllerState {
    case requestingBalanceAndPriceOnAppearance
    case readyToGetSwypeCode
    case requestingBalanceAndPriceThenGetSwypeCode
    case gettingSwypeCode
    case waitingForCode
    case didReceiveCode
    case waitingForCircle
    case waitingToStartSwypeCode
    case detectingSwypeCode
    case finishingWithoutDetectedSwypeCode
    case swypeCodeDetected
    case finishingWithDetectedSwypeCode
    case submittingMediaHash
    case confirmingMediaHashSubmission
}
