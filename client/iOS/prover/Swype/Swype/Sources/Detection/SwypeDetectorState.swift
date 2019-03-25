import Foundation

enum SwypeDetectorState: Int32 {
    case waitingForCode = 0
    case waitingForCircle = 1
    case waitingToStartSwypeCode = 2
    case detectingSwypeCode = 3
    case swypeCodeDetected = 4
    
    static func < (lhs: SwypeDetectorState, rhs: SwypeDetectorState) -> Bool {
        return lhs.rawValue < rhs.rawValue
    }
}
