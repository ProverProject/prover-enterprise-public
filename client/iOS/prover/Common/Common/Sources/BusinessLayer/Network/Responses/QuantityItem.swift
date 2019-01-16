import Foundation

public struct QuantityItem: Decodable {
    public var mosaicId: Mosaic
    public var quantity: Int64

    public var floatQuantity: Double {
        return Double(quantity) / 1000000
    }
}
