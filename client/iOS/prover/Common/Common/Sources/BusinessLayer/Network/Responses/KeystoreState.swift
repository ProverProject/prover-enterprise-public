import Foundation

public enum KeystoreState: String, Decodable {
    case locked
    case unlocked
    case uninitialized
    case corrupted
}
