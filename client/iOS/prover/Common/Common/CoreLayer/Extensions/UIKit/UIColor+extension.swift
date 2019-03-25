import UIKit

public extension UIColor {
    /// UIColor initialisation from bytes
    ///
    /// - Parameters:
    ///   - byteRed: Red channel byte value
    ///   - byteGreen: green channel byte value
    ///   - byteBlue: Blue channel byte value
    ///   - byteAlpha: Alpha channel byte value
    convenience init(byteRed: UInt8, byteGreen: UInt8, byteBlue: UInt8, byteAlpha: UInt8) {
        self.init(red: CGFloat(byteRed) / 255,
                       green: CGFloat(byteGreen) / 255,
                       blue: CGFloat(byteBlue) / 255,
                       alpha: CGFloat(byteAlpha) / 255)
    }
    
    /// UIColor initialisation from hex string
    ///
    /// - Parameters:
    ///   - hexString: Hex string of color. Usually starts with #
    ///   - alpha: Alpha channel
    convenience init(hexString: String, alpha: CGFloat = 1.0) {
        
        // Convert hex string to an integer
        var hexInt: UInt32 = 0
        // Create scanner
        let scanner: Scanner = Scanner(string: hexString)
        // Tell scanner to skip the # character
        scanner.charactersToBeSkipped = CharacterSet(charactersIn: "#")
        // Scan hex value
        scanner.scanHexInt32(&hexInt)
        
        let red = CGFloat((hexInt & 0xff0000) >> 16) / 255.0
        let green = CGFloat((hexInt & 0xff00) >> 8) / 255.0
        let blue = CGFloat((hexInt & 0xff) >> 0) / 255.0
        let alpha = alpha
        
        self.init(red: red, green: green, blue: blue, alpha: alpha)
    }
}
