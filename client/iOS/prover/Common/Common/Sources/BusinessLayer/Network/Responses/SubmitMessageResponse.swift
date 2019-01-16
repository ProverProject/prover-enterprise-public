import Foundation

public struct SubmitMessageResponse: Decodable {
    public var txhash: String
    public var referenceBlockHeight: UInt
    public var referenceBlockHash: String
    public var messageSignature: String

    public var qrCodeData: Data {
        let bigEndianBlockHeight = CFSwapInt32HostToBig(UInt32(referenceBlockHeight))
        let bigEndianBlockHeightArray = withUnsafeBytes(of: bigEndianBlockHeight) { Array($0) }
        let referenceBlockHashArray = referenceBlockHash.hex2Bytes
        let messageSignatureArray = messageSignature.hex2Bytes
        let summaryBytesArray = bigEndianBlockHeightArray + referenceBlockHashArray + messageSignatureArray

        return Data(summaryBytesArray)
    }
}
