import Foundation

public enum EstimateFeeRequestType: String, Codable {
    case requestSwypeCode = "request-swype-code"
    case submitMessage = "submit-message"
    case submitMediaHash = "submit-media-hash"
}
