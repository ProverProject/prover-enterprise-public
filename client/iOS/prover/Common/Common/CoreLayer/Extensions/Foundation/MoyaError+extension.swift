import Foundation
import Moya

public extension MoyaError {
    public var urlError: URLError? {
        switch self {
        case .underlying(let underlying, _):
            return underlying as? URLError
        default:
            return nil
        }
    }
}
