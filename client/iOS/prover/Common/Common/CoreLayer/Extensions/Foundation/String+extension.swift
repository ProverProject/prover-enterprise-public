import Foundation

extension String: LocalizedError {
    public var errorDescription: String? { return self }
}

extension String {

    var doubleValue: Double {
        return Double(int64Value)
    }
    
    var int64Value: UInt64 {
        let scanner = Scanner(string: self)
        var scannerOutput: UInt64 = 0
        _ = scanner.scanHexInt64(&scannerOutput)
        
        return scannerOutput
    }
}
