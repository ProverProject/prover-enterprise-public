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

public extension String {
    func simpleFormatting(mediumFont: UIFont, boldFont: UIFont) -> NSAttributedString {
        let mediumFontAttrs: [NSAttributedString.Key: Any] = [.font: mediumFont]
        let boldFontAttrs: [NSAttributedString.Key: Any] = [.font: boldFont]
        let underlineStyleAttrs: [NSAttributedString.Key: Any] = [.underlineStyle: NSUnderlineStyle.single.rawValue]
        
        let attrString = NSMutableAttributedString(string: self, attributes: mediumFontAttrs)
        
        for range in attrString.deleteOccurenciesOf("_") {
            attrString.setAttributes(boldFontAttrs, range: range)
        }
        
        for range in attrString.deleteOccurenciesOf("+") {
            attrString.setAttributes(underlineStyleAttrs, range: range)
        }
        
        return attrString
    }
}

private extension NSMutableAttributedString {
    
    func deleteOccurenciesOf(_ substring: String) -> [NSRange] {
        var indices: [Int] = []
        let str = string
        var totalSubstringLength = 0
        var searchStartIndex = str.startIndex
        
        while searchStartIndex < str.endIndex,
            let range = str.range(of: substring, range: searchStartIndex..<str.endIndex),
            !range.isEmpty
        {
            let index = str.distance(from: str.startIndex, to: range.lowerBound)
            
            indices.append(index - totalSubstringLength)
            searchStartIndex = range.upperBound
            totalSubstringLength += substring.count
        }
        
        if !indices.isEmpty && indices.count % 2 != 0 {
            indices.removeLast()
        }
        
        for index in indices {
            deleteCharacters(in: NSRange(location: index, length: substring.count))
        }
        
        let indexPairs = indices.chunked(into: 2)
        let ranges = indexPairs.map { pair in
            NSRange(location: pair[0], length: pair[1] - pair[0])
        }
        
        return ranges
    }
}

private extension Array {
    func chunked(into size: Int) -> [[Element]] {
        return stride(from: 0, to: count, by: size).map {
            Array(self[$0 ..< Swift.min($0 + size, count)])
        }
    }
}
