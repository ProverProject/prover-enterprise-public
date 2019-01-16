import Foundation

public struct SwypeCodeRequestResponse: Decodable {
    public var swypeID: UInt64?
    public var swypeSequence: String?
    public var swypeSeed: String?

    enum CodingKeys: String, CodingKey {
        case swypeID = "swype-id"
        case swypeSequence = "swype-sequence"
        case swypeSeed = "swype-seed"
    }
}
