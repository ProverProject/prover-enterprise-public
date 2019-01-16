import Foundation

extension NSDecimalNumber {
    func localizedPrice() -> String {
        let currencyFormatter = NumberFormatter()
        
        currencyFormatter.locale = .current
        currencyFormatter.minimumFractionDigits = 0
        currencyFormatter.maximumFractionDigits = 2
        currencyFormatter.numberStyle = .currency
        
        return currencyFormatter.string(from: self) ?? ""
    }
}
