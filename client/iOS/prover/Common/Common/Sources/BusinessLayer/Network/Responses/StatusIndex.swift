import Foundation

public struct StatusIndex: Decodable {
    public var indexTopBlock: UInt64?
    public var networkTopBlock: UInt64?
    public var targetTopBlock: UInt64?
}
