import Foundation

public struct ErrorResponse: Decodable {
    public var code: Int
    public var message: String
    public var data: ErrorDataResponse?
}
