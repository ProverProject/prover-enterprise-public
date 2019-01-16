import Foundation

public struct ConfirmSubmissionResponse: Decodable {
    public var confirmations: UInt
    public var height: UInt64?
}
