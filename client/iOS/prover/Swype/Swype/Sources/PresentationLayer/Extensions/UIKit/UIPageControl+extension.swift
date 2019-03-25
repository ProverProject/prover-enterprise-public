import UIKit

extension UIPageControl {

    func setNumberOfPages(number: Int) {
        numberOfPages = number
    }

    func setCurrentStep(_ value: Int) {
        guard value > -1 else {
            reset()
            return
        }
        setCurrentStepColor(activeColor)
        currentPage = value
    }

    func setLastStep() {
        setCurrentStep(numberOfPages - 1)
    }
    
    func reset() {
        setTintColor(defaultColor)
        setCurrentStepColor(defaultColor)
    }

    func paintToErrorColor() {
        setTintColor(.red)
        setCurrentStepColor(.red)
    }
    
    private var defaultColor: UIColor {
        return UIColor(red: 1, green: 1, blue: 1, alpha: 0.5)
    }

    private var activeColor: UIColor {
        return .green
    }

    private func setCurrentStepColor(_ color: UIColor) {
        currentPageIndicatorTintColor = color
    }

    private func setTintColor(_ color: UIColor) {
        pageIndicatorTintColor = color
    }
}
