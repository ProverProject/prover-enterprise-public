import Foundation
import Reachability

public class NetworkReachability {
    private let reachability = Reachability()!
    
    public init(stateMachine: SuspendableStateMachine) {
        
        reachability.whenUnreachable = { [weak wStateMachine = stateMachine] _ in
            wStateMachine?.suspendState()
        }
        
        reachability.whenReachable = { [weak wStateMachine = stateMachine] _ in
            wStateMachine?.resumeState()
        }
        
        try! reachability.startNotifier()
    }
}
