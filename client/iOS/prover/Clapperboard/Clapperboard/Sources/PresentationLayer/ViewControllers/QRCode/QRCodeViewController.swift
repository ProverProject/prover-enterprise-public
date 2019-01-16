import UIKit
import Common
import AVFoundation
import BigInt

class QRCodeViewController: UIViewController {
    
    // MARK: - IBOutlet
    @IBOutlet weak var cameraView: UIView!
    
    @IBOutlet weak var qrText: UILabel!
    @IBOutlet weak var qrImage: UIImageView!
    @IBOutlet weak var logoQRImage: UIImageView!
    
    private let placeholderImage = #imageLiteral(resourceName: "symbol")
    private lazy var qrCodeGenerator: QRGeneratorService = {
        let version = SharedSettings.shared.appVersion
        let backColor: UIColor = version == .new ? .black : .clear
        let frontColor: UIColor = version == .new ? .clear : .black
        
        let qrGenerator = QRGeneratorService(backColor: backColor, frontColor: frontColor)
        return qrGenerator
    }()
    
    private lazy var maskImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.alpha = 0.0
        imageView.frame = CGRect(origin: .zero, size: self.logoQRImage.frame.size)
        imageView.image = placeholderImage
        imageView.contentMode = .center
        return imageView
    }()
    
    //Camera Capture requiered properties
    private var videoDataOutput: AVCaptureVideoDataOutput!
    private var videoDataOutputQueue: DispatchQueue!
    private var previewLayer:AVCaptureVideoPreviewLayer!
    private var captureDevice : AVCaptureDevice!
    private let session = AVCaptureSession()
    private var currentFrame: CIImage!
    private var done = false
    private let version = SharedSettings.shared.appVersion
    
    deinit {
        debugPrint("DEINITED VC: ", self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.logoQRImage.tintColor = .red
        self.logoQRImage.image = placeholderImage
        self.logoQRImage.contentMode = .center
        
        self.qrImage.alpha = 0.0
        self.qrImage.tintColor = version == .new ? .white : UIColor(hexString: "3B3D47")
        self.qrText.alpha = 0.0
        self.qrText.textColor = version == .new ? .white : UIColor(hexString: "3B3D47")
        
        if version == .new {
            self.setupAVCapture()
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        self.setupNavigationBarAppearance()
        if !done && version == .new {
            session.startRunning()
        }
    }
    
    private func setupNavigationBarAppearance() {
        guard SharedSettings.shared.appVersion == .new else { return }
        
        navigationController?.setNavigationBarHidden(false, animated: false)
        
        if let navigationBar = navigationController?.navigationBar {
            // Make navigation bar transparent
            navigationBar.setBackgroundImage(UIImage(), for: .default)
            navigationBar.shadowImage = UIImage()
            navigationBar.isTranslucent = true
            
            // Make the white title
            navigationBar.barStyle = .black
            
            // Enlarge title
            navigationBar.titleTextAttributes =
                [.font: UIFont.boldSystemFont(ofSize: 21)]
        }
    }

    /// QR Code generating
    ///
    /// - Parameter string: input text
    func generateQRCode(message: String, qrCodeData: Data, completion: @escaping () -> Void = {  }) {

        let bigNum = BigUInt(qrCodeData)
        let description = bigNum.description
        
        guard let image = qrCodeGenerator.generateQRCode(fromString: description, withImageSize: self.qrImage.frame.size) else { return }
        
        UIView.animate(withDuration: 0.3, animations: {
            self.logoQRImage.alpha = 0.0
        }) { (_) in
            self.qrText.text = message
            
            self.qrImage.image = image.withRenderingMode(.alwaysTemplate)
            self.logoQRImage.contentMode = .scaleAspectFit
            self.logoQRImage.image = image.withRenderingMode(.alwaysTemplate)
            
            self.logoQRImage.mask = self.maskImageView
            
            UIView.animate(withDuration: 0.3, animations: {
                self.qrText.alpha = 1.0
                self.qrImage.alpha = 1.0
                self.maskImageView.alpha = 1.0
                self.logoQRImage.alpha = 1.0
            }, completion: { (_) in
                completion()
            })
        }
    }
    
    public func generateTestQR(_ completion: @escaping () -> Void = {  }) {
        let dataArray = "wertedfghdfhth fg hd fgh"
        
        guard let image = qrCodeGenerator.generateQRCode(fromString: dataArray, withImageSize: self.qrImage.frame.size) else { return }
        
        UIView.animate(withDuration: 0.3, animations: {
            self.logoQRImage.alpha = 0.0
        }) { (_) in
            self.qrText.text = "qewrj jcjf gjdfsjg dfjh ;jdf;h jfgskhj "
            
            self.qrImage.image = image.withRenderingMode(.alwaysTemplate)
            self.logoQRImage.contentMode = .scaleAspectFit
            self.logoQRImage.image = image.withRenderingMode(.alwaysTemplate)
            
            self.logoQRImage.mask = self.maskImageView
            
            UIView.animate(withDuration: 0.3, animations: {
                self.qrText.alpha = 1.0
                self.qrImage.alpha = 1.0
                self.maskImageView.alpha = 1.0
                self.logoQRImage.alpha = 1.0
            }, completion: { (_) in
                completion()
            })
        }
    }
}

// AVCaptureVideoDataOutputSampleBufferDelegate protocol and related methods
extension QRCodeViewController: AVCaptureVideoDataOutputSampleBufferDelegate {
    private func setupAVCapture(){
        session.sessionPreset = AVCaptureSession.Preset.vga640x480
        guard let device = AVCaptureDevice
            .default(AVCaptureDevice.DeviceType.builtInWideAngleCamera,
                     for: .video,
                     position: AVCaptureDevice.Position.back) else{
                        return
        }
        captureDevice = device
        beginSession()
        done = true
    }
    
    private func beginSession(){
        var deviceInput: AVCaptureDeviceInput!
        do {
            deviceInput = try AVCaptureDeviceInput(device: captureDevice)
            guard deviceInput != nil else {
                print("error: cant get deviceInput")
                return
            }
            
            if self.session.canAddInput(deviceInput){
                self.session.addInput(deviceInput)
            }
            
            videoDataOutput = AVCaptureVideoDataOutput()
            videoDataOutput.alwaysDiscardsLateVideoFrames = true
            videoDataOutputQueue = DispatchQueue(label: "VideoDataOutputQueue")
            videoDataOutput.setSampleBufferDelegate(self, queue:self.videoDataOutputQueue)
            
            if session.canAddOutput(self.videoDataOutput){
                session.addOutput(self.videoDataOutput)
            }
            
            videoDataOutput.connection(with: .video)?.isEnabled = true
            
            self.previewLayer = AVCaptureVideoPreviewLayer(session: self.session)
            self.previewLayer.videoGravity = .resizeAspectFill
            
            let rootLayer: CALayer = self.cameraView.layer
            rootLayer.masksToBounds = true
            self.previewLayer.frame = rootLayer.bounds
            rootLayer.addSublayer(self.previewLayer)
            session.startRunning()
        } catch let error as NSError {
            deviceInput = nil
            print("error: \(error.localizedDescription)")
        }
    }
    
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        currentFrame = self.convertImageFromCMSampleBufferRef(sampleBuffer)
    }
    
    // clean up AVCapture
    private func stopCamera(){
        session.stopRunning()
        done = false
    }
    
    private func convertImageFromCMSampleBufferRef(_ sampleBuffer:CMSampleBuffer) -> CIImage{
        let pixelBuffer: CVPixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer)!
        let ciImage:CIImage = CIImage(cvImageBuffer: pixelBuffer)
        return ciImage
    }
}
