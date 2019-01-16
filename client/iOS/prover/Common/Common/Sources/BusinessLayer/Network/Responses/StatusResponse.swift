import Foundation

public struct StatusResponse: Decodable {
    public var index: StatusIndex
    public var keystore: StatusKeystore
}
