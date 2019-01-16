import UIKit

class VideoPreviewView: UIImageView {
    override class var layerClass: AnyClass {
        return AVCaptureVideoPreviewLayer.self
    }
}
