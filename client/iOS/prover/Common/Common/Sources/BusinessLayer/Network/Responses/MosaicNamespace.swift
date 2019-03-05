import Foundation

public enum MosaicNamespace: RawRepresentable, Decodable {
    public typealias RawValue = String
    
    case nem
    case prover
    case other(String)
    
    public var rawValue: String {
        switch self {
        case .other(let namespace):
            return namespace
        case .nem:
            return "nem"
        case .prover:
            return "prover"
        }
    }
    
    public init?(rawValue: String) {
        switch rawValue {
        case "nem": self = .nem
        case "prover": self = .prover
        default: self = .other(rawValue)
        }
    }
    
    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let namespace = try container.decode(String.self)
        self = MosaicNamespace(rawValue: namespace) ?? .other(namespace)
    }
}
