import UIKit

extension UIPageControl {
    
    func update(by state: SwypeViewControllerState) {
        
        switch state {
        case .finishingWithoutDetectedSwypeCode,
             .finishingWithDetectedSwypeCode:
            break
        case .requestingBalanceAndPriceOnAppearance,
             .readyToGetSwypeCode,
             .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .waitingForCode,
             .didReceiveCode,
             .waitingForCircle,
             .waitingToStartSwypeCode:
            setAnimatedVisibility(visible: false)
        case .detectingSwypeCode:
            reset()
            setAnimatedVisibility(visible: true)
        case .swypeCodeDetected:
            setCurrentStepColor(activeColor)
            setTintColor(activeColor)
            setAnimatedVisibility(visible: false)
        case .submittingMediaHash,
             .confirmingMediaHashSubmission:
            setAnimatedVisibility(visible: false)
            setCurrentStepColor(activeColor)
        }
    }
    
    var defaultColor: UIColor {
        return UIColor(red: 1, green: 1, blue: 1, alpha: 0.5)
    }
    
    var activeColor: UIColor {
        return .green
    }
    
    func setNumberOfPages(_ number: Int) {
        DispatchQueue.main.async { [weak self] in
            self?.numberOfPages = number
        }
    }
    
    func setCurrentStep(_ value: Int) {
        guard value > -1 else {
            reset()
            return
        }
        setCurrentStepColor(activeColor)
        DispatchQueue.main.async { [weak self] in
            self?.currentPage = value
        }
    }
    
    func setCurrentStepColor(_ color: UIColor) {
        DispatchQueue.main.async { [weak self] in
            self?.currentPageIndicatorTintColor = color
        }
    }
    
    func setTintColor(_ color: UIColor) {
        DispatchQueue.main.async { [weak self] in
            self?.pageIndicatorTintColor = color
        }
    }
    
    func reset() {
        setTintColor(defaultColor)
        setCurrentStepColor(defaultColor)
    }
}
