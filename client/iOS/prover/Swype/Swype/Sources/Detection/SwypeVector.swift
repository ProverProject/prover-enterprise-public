import Foundation

enum SwypeVector: Int {

    case zero = 0
    case down = 1
    case leftDown = 2
    case left = 3
    case leftUp = 4
    case up = 5
    case rightUp = 6
    case right = 7
    case rightDown = 8

    var x: Int {
        switch self {
        case .leftDown, .left, .leftUp:
            return -1
        case .up, .zero, .down:
            return 0
        case .rightUp, .right, .rightDown:
            return 1
        }
    }

    var y: Int {
        switch self {
        case .leftUp, .up, .rightUp:
            return -1
        case .left, .zero, .right:
            return 0
        case .rightDown, .down, .leftDown:
            return 1
        }
    }
}
