/*
import Foundation

class FramesPerSecond {
    private var timestamps: [CMTime] = []
    private let maxCapacity: Int = 200 // maximum number of frames
    
    private var lastTimestamp: CMTime?
    
    func addTimestamp(_ timestamp: CMTime) {
        if timestamps.count == maxCapacity {
            self.removeFirstTimestamp()
        }
        self.timestamps.append(timestamp)
        self.setLastTimestamp(timestamp)
    }
    
    private func setLastTimestamp(_ timestamp: CMTime) {
        guard let lastTime = self.lastTimestamp else {
            self.lastTimestamp = timestamp
            return
        }
        
        let differenceValue = timestamp.value - lastTime.value
        
        if differenceValue > 1 * Int64(lastTime.timescale) {
            guard let index = self.timestamps.index(where: { $0 == lastTime }) else { return }
            let frames = Int64(self.timestamps.count - 1 - index)
            
            guard frames != 0 else { return }
            
            //let fps = differenceValue / frames
            let resultFps = 1 * Int64(lastTime.timescale) * frames / differenceValue
            
            self.lastTimestamp = timestamp
            
            //debugPrint("[VideoRecorder] RESULT FPS", resultFps)
        }
    }
    
    func removeAllTimestamps() {
        self.timestamps.removeAll()
    }
    
    private func removeFirstTimestamp() {
        self.timestamps.remove(at: 0)
    }
}
 */
