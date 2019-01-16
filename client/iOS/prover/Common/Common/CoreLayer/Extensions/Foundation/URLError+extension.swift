import Foundation

public extension URLError {
    public var urlErrorCode: URLError.Code {
        return URLError.Code(rawValue: errorCode)
    }
}
