import Foundation

public struct BalanceResponse: Decodable {
    public var address: String
    public var balance: [QuantityItem]
}
