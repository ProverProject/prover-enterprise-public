import UIKit

class InfoView: UIView {
    
    // MARK: - IBOutlet
    @IBOutlet private weak var titleLabel: UILabel!
    @IBOutlet private weak var messageLabel: UILabel!

    private var prevState: SwypeViewControllerState?

    public var titleText: String! {
        get {
            return titleLabel.text
        }
        set {
            titleLabel.text = newValue
        }
    }

    public var titleColor: UIColor! {
        get {
            return titleLabel.textColor
        }
        set {
            titleLabel.textColor = newValue
        }
    }

    public var message: String! {
        get {
            return messageLabel.text
        }
        set {
            messageLabel.text = newValue
        }
    }
}

enum VideoSubmitterStatus {
    case getInfo
    case createTransaction
    case sendSubmitRequest
    case endSuccess
    case endError
}

extension InfoView {
    
    func update(by state: SwypeViewControllerState, isNotEnoughFunds: Bool) {
        
        guard prevState != state else {
            return
        }

        print("[InfoView] switch to \(state)")

        prevState = state
        
        switch state {
        case .requestingBalanceAndPriceOnAppearance:
            titleLabel.setAnimatedText("Requesting balance and price...")

        case .readyToGetSwypeCode:
            if isNotEnoughFunds {
                titleLabel.setAnimatedText("Not enough funds in the account!")
            }

        case .requestingBalanceAndPriceThenGetSwypeCode:
            titleLabel.setAnimatedText("Purchasing swype code...")

        case .gettingSwypeCode:
            break

        case .waitingForCode:
            titleLabel.setAnimatedText("Downloading swype code...")

        case .didReceiveCode:
            titleLabel.setAnimatedText("Swype code received!")

        case .waitingForCircle:
            break

        case .waitingToStartSwypeCode:
            titleLabel.setAnimatedText("Stop moving the device")

        case .detectingSwypeCode:
            break

        case .finishingWithoutDetectedSwypeCode:
            titleLabel.setAnimatedText("Detection is NOT finished!")

        case .swypeCodeDetected:
            titleLabel.setAnimatedText("Detection is complete!")

        case .finishingWithDetectedSwypeCode:
            titleLabel.setAnimatedText("Finishing record")

        case .submittingMediaHash:
            titleLabel.setAnimatedText("Submitting media hash")

        case .confirmingMediaHashSubmission:
            titleLabel.setAnimatedText("Confirming media hash submission")
        }
    }
}
