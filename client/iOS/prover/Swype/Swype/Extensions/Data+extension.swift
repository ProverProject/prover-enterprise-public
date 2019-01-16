import Foundation

extension Data {
    var hexDescription: String {
        return reduce("") {$0 + String(format: "%02x", $1)}
    }
    
    var bytesDescription: [UInt8] {
        return [UInt8](self)
    }
}
