import Foundation

public protocol SuspendableStateMachine: class {
    func suspendState()
    func resumeState()
}
