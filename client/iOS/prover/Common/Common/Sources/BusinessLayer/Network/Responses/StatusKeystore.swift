import Foundation

public struct StatusKeystore: Decodable {
    public var address: String?
    public var state: KeystoreState
}
