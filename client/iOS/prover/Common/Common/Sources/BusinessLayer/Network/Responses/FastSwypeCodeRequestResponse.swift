import Foundation

public struct FastSwypeCodeRequestResponse: Decodable {
    public var referenceBlockHeight: UInt64
    public var swypeID: UInt64
    public var swypeSequence: String
    public var swypeSeed: String

    enum CodingKeys: String, CodingKey {
        case referenceBlockHeight = "reference-block-height"
        case swypeID = "swype-id"
        case swypeSequence = "swype-sequence"
        case swypeSeed = "swype-seed"
    }
}
