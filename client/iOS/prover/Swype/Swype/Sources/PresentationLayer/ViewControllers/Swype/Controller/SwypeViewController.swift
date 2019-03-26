import UIKit
import AVFoundation
import Accelerate
import Photos
import PromiseKit
import Moya
import Common

protocol VideoProcessorDelegate: class {
    var controllerState: SwypeViewControllerState { get }
    func recordFailed()
    func recordFinished()
    func metadataSaved(status: AVAssetExportSession.Status)
    func recorderDidUpdateFps(_ fps: Double)
    func detectorDidUpdateFps(_ fps: Double)
}

class SwypeViewController: BaseViewController, SuspendableStateMachine {

    // MARK: - IBOutlet
    @IBOutlet weak var detectorView: SwypeDetectorView!
    @IBOutlet weak var videoPreviewView: VideoPreviewView!
    @IBOutlet weak var recordButton: UIButton!
    @IBOutlet weak var walletButton: UIButton!
    @IBOutlet weak var settingsButton: UIButton!
    @IBOutlet weak var guideButton: UIButton!
    
    @IBOutlet weak var balanceLabel: BalanceLabel!
    @IBOutlet weak var proverRotatingView: SpinnerView!
    @IBOutlet weak var priceLabelHolder: UIView!
    @IBOutlet weak var priceLabelContainer: PriceLabelContainer!
    @IBOutlet weak var priceLabel: PriceLabel!
    
    @IBOutlet weak var fpsLabel: UILabel!
    
    private var isNavigatingToSettings: Bool = false
    
    private var videoCaptureFps: Double = 0
    private var videoDetectionFps: Double = 0

    private var balance: Double = 0
    private var price: Double = 0

    private func isNotEnoughFunds() -> Bool {
        return price == 0 || balance < price
    }

    private var swypeCodeRequestTxHash: String?
    private var submitMediaHashTxHash: String?
    private var referenceBlockHeight: UInt64?
    
    private var isSwypeCodeDownloaded = false
    private var discardVideoHashSubmission = false

    private var fastSwypeCode: String?
    
    private var pendingDownloadingSwypeCode = false
    private var pendingConfirmingVideoHashSubmission = false
    private var reachability: NetworkReachability!
    private var isStateSuspended: Bool = false

    private let allowAccessToCameraMessage = """
            PROVER Swype ID needs access to your camera to take videos.
            
            Please go to Settings > PROVER Swype ID and set Camera to ON.
            """

    private let allowAccessToPhotosMessage = """
            PROVER Swype ID needs access to photos to record videos.
            
            Please go to Settings > PROVER Swype ID and set Photos to ON.
            """

    // MARK: - IBAction
    @IBAction func recordButtonAction(_ sender: UIButton) {
        switchState()
    }
    
    @IBAction func walletButtonAction(_ sender: UIButton) {
        routeToWallet()
    }
    
    @IBAction func settingsButtonAction(_ sender: UIButton) {
        routeToSettings()
    }
    
    @IBAction func guideButtonAction(_ sender: UIButton) {
        routeToGuide()
    }

    // To be called by self.reachability, see its type declaration for details
    func suspendState() {

        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .submittingVideoHash,
             .confirmingVideoHashSubmission:
            isStateSuspended = true
            
        case .recording:
            isStateSuspended = !isSwypeCodeDownloaded
            
        case .readyToPurchaseSwypeCode,
             .requestingBalanceAndPriceThenPurchase,
             .purchasingSwypeCode,
             .finishingRecord:
            isStateSuspended = false
        }
    }

    // To be called by self.reachability, see its type declaration for details
    func resumeState() {

        // If we are in .readyToGetSwypeCode then we anyway refresh the balance
        guard isStateSuspended || controllerState == .readyToPurchaseSwypeCode else {
            return
        }

        isStateSuspended = false

        switch controllerState {
        case .readyToPurchaseSwypeCode:
            controllerState = .requestingBalanceAndPriceOnAppearance
            requestBalanceAndPrice()

        case .requestingBalanceAndPriceOnAppearance:
            requestBalanceAndPrice()

        case .recording:
            if Settings.useFastSwypeCode {
                requestFastSwypeCode()
            }
            else {
                if !isSwypeCodeDownloaded && !provider.isRequesting && !pendingDownloadingSwypeCode {
                    downloadSwypeCode()
                }
            }

        case .submittingVideoHash:
            if Settings.useFastSwypeCode {
                submitMediaHashForFastSwypeCode()
            }
            else {
                submitMediaHashForNormalSwypeCode()
            }

        case .confirmingVideoHashSubmission:
            if !provider.isRequesting && !pendingConfirmingVideoHashSubmission {
                confirmMediaHashSubmission()
            }

        case .requestingBalanceAndPriceThenPurchase,
             .purchasingSwypeCode,
             .finishingRecord:
            break
        }
    }

    private func revertState() {
        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenPurchase,
             .purchasingSwypeCode,
             .submittingVideoHash,
             .confirmingVideoHashSubmission:
            controllerState = .readyToPurchaseSwypeCode

        case .readyToPurchaseSwypeCode,
             .recording,
             .finishingRecord:
            break
        }
    }

    private func switchState() {
        
        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance:
            controllerState = .readyToPurchaseSwypeCode

        case .readyToPurchaseSwypeCode:
            controllerState = .requestingBalanceAndPriceThenPurchase
            requestBalanceAndPrice()

        case .requestingBalanceAndPriceThenPurchase:
            controllerState = .purchasingSwypeCode

            if Settings.useFastSwypeCode {
                requestFastSwypeCode()
            }
            else {
                purchaseSwypeCode()
            }

        case .purchasingSwypeCode:
            controllerState = .recording
            videoProcessor.startRecord()

            if Settings.useFastSwypeCode {
                let swypeLength = fastSwypeCode!.filter { $0 != "*" }.count
                videoProcessor.setSwypeCode(fastSwypeCode!)
                detectorView.setSwypeLength(to: swypeLength)
            }
            else {
                downloadSwypeCode()
            }

        case .recording:
            let stopRecordAction: (Bool) -> Void = { [weak self] discard in
                self?.discardVideoHashSubmission = discard
                self?.controllerState = .finishingRecord
                self?.videoProcessor.stopRecord()
                
                if discard {
                    self?.showNotVerifiedView()
                }
            }
            
            if detectorView.isSwypeCodeDetected {
                stopRecordAction(false)
            }
            else {
                warnAboutSwypeCodeNotEntered(
                    send: {
                        stopRecordAction(false)
                    },
                    discard: {
                        stopRecordAction(true)
                    })
            }

        case .finishingRecord:
            if discardVideoHashSubmission {
                controllerState = .requestingBalanceAndPriceOnAppearance
                requestBalanceAndPrice()
            }
            else {
                controllerState = .submittingVideoHash

                if Settings.useFastSwypeCode {
                    submitMediaHashForFastSwypeCode()
                }
                else {
                    submitMediaHashForNormalSwypeCode()
                }
            }

        case .submittingVideoHash:
            controllerState = .confirmingVideoHashSubmission
            confirmMediaHashSubmission()

        case .confirmingVideoHashSubmission:
            controllerState = .requestingBalanceAndPriceOnAppearance
            requestBalanceAndPrice()
        }
    }

    private func showNotVerifiedView() {
        self.detectorView.discardMovement { [weak self] in
            guard let `self` = self else { return }
            
            let frame = CGRect(x: 0, y: 0, width: self.view.frame.width-60, height: 120)
            let nvView = NotVerifiedView(frame: frame)
            nvView.translatesAutoresizingMaskIntoConstraints = false
            
            self.view.addSubview(nvView)
            
            nvView.leadingAnchor.constraint(equalTo: self.view.leadingAnchor, constant: 30).isActive = true
            self.view.trailingAnchor.constraint(equalTo: nvView.trailingAnchor, constant: 30).isActive = true
            nvView.topAnchor.constraint(equalTo: self.walletButton.bottomAnchor, constant: 16).isActive = true
            
            self.removeNotVerifiedView()
        }
    }
    
    private func removeNotVerifiedView() {
        guard let nvView = self.view.subviews.first(where: { $0 is NotVerifiedView }) else {
            return
        }
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            UIView.animate(withDuration: 0.5, animations: {
                nvView.alpha = 0
            }) { (_) in
                nvView.removeFromSuperview()
            }
        }
    }
    
    // MARK: - Dependencies
    var videoProcessor: VideoProcessor! {
        willSet {
            if newValue == nil {
                print("[SwypeViewController] self.videoProcessor will set to nil!")
            }
        }
        didSet {
            if videoProcessor == nil {
                print("[SwypeViewController] self.videoProcessor did set to nil!")
            }
        }
    }

    // MARK: - Private properties
    private var isAuthorizedToUseCamera: Bool {
        return AVCaptureDevice.authorizationStatus(for: .video) == .authorized
    }

    private var isAuthorizedToUsePhotos: Bool {
        return PHPhotoLibrary.authorizationStatus() == .authorized
    }

    private var swypeBlock = ""

    public var prevControllerState: SwypeViewControllerState?
    public var controllerState: SwypeViewControllerState = .readyToPurchaseSwypeCode {
        didSet {
            prevControllerState = oldValue
            updateState()
        }
    }

    private var videoProcessorDidLayoutSubviews: Bool = false
    
    // MARK: - View controller lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()

        reachability = NetworkReachability(stateMachine: self)
    }

    override func viewWillAppear(_ animated: Bool) {
        print("[SwypeViewController] viewWillAppear")
        super.viewWillAppear(animated)

        fpsLabel.isHidden = !Settings.showFps
        navigationController!.setNavigationBarHidden(true, animated: true)

        guard isAuthorizedToUseCamera else {
            skipSwypeViewController(message: allowAccessToCameraMessage)
            return
        }

        guard isAuthorizedToUsePhotos else {
            skipSwypeViewController(message: allowAccessToPhotosMessage)
            return
        }
        
        restoreAnimation()
        
        if videoProcessor == nil {
            videoProcessor = VideoProcessor(
                videoPreviewView: videoPreviewView,
                coordinateDelegate: detectorView,
                delegate: self)
            videoProcessor.startCapture()
            
            if controllerState == .readyToPurchaseSwypeCode {
                controllerState = .requestingBalanceAndPriceOnAppearance
                requestBalanceAndPrice()
            }
        }
    }
    
    private func restoreAnimation() {
        
        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenPurchase,
             .purchasingSwypeCode,
             .submittingVideoHash,
             .confirmingVideoHashSubmission:
            proverRotatingView.startAnimation()
            
        case .recording:
            if !isSwypeCodeDownloaded {
                proverRotatingView.startAnimation()
            }
            
        case .readyToPurchaseSwypeCode,
             .finishingRecord:
            break
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        print("[SwypeViewController] viewDidDisappear")
        super.viewDidDisappear(animated)
        
        if isNavigatingToSettings {
            isNavigatingToSettings = false
            
            videoProcessor?.stopCapture()
            videoProcessor = nil
        }
    }

    override var preferredStatusBarStyle : UIStatusBarStyle {
        return .lightContent
    }

    override func handleWillEnterForeground() {
        
        guard let videoProcessor = self.videoProcessor else {
            // We have gone to the settings screen so dropped video processor
            return
        }
        
        print("[SwypeViewController] handleWillEnterForeground()")
        
        restoreAnimation()
        
        if controllerState == .readyToPurchaseSwypeCode {
            controllerState = .requestingBalanceAndPriceOnAppearance
            requestBalanceAndPrice()
        }
        
        videoProcessor.startCapture()
    }

    override func handleDidEnterBackground() {
        
        guard let videoProcessor = self.videoProcessor else {
            // We have gone to the settings screen so dropped video processor
            return
        }
        
        print("[SwypeViewController] handleDidEnterBackground()")
        
        if videoProcessor.isRecording {
            switchState()
        }
        
        videoProcessor.stopCapture()
    }

    private func requestBalanceAndPrice() {

        self.proverRotatingView.startAnimation()

        let provider = self.provider

        let estimateSubmitHashFunc = { [weak self] (result: BalanceResponse) -> Promise<[QuantityItem]> in

            let balanceItem = result.balance.first {
                $0.mosaicId.namespaceId == .prover
            }

            self?.balance = balanceItem?.floatQuantity ?? 0
            self?.price = 0

            let estimationEntry = Settings.useFastSwypeCode
                    ? NetworkAPI.estimate_submit_media_hash_for_fast_swype_fee
                    : NetworkAPI.estimate_submit_media_hash_for_normal_swype_fee

            return provider.promise(target: estimationEntry)
        }

        let estimateRequestSwypeCodeFunc = { [weak self] (result: [QuantityItem]) -> Promise<[QuantityItem]> in

            let quantityItem = result.first {
                $0.mosaicId.namespaceId == .prover
            }

            self?.price += quantityItem?.floatQuantity ?? 0

            return provider.promise(target: NetworkAPI.estimate_request_swype_code_fee)
        }

        let doneFunc = { [weak self] (result: [QuantityItem]) in

            let quantityItem = result.first {
                $0.mosaicId.namespaceId == .prover
            }

            self?.price += quantityItem?.floatQuantity ?? 0

            let initialRequestingBalanceAndPrice = self?.controllerState == .requestingBalanceAndPriceOnAppearance
            let isNotEnoughFunds = self?.isNotEnoughFunds() ?? false

            if initialRequestingBalanceAndPrice || isNotEnoughFunds {
                self?.proverRotatingView.stopAnimation()
            }

            if isNotEnoughFunds {
                self?.revertState()
            }
            else {
                self?.switchState()
            }
        }

        let catchFunc = { [weak self] (error: Error) in

            if !(self?.isStateSuspended ?? false) {
                self?.proverRotatingView.stopAnimation()
                self?.revertState()
                self?.requestFailed(error)
            }
        }

        if Settings.useFastSwypeCode {
            provider.promise(target: NetworkAPI.get_balance)
                    .then(estimateSubmitHashFunc)
                    .done(doneFunc)
                    .catch(catchFunc)
        }
        else {
            provider.promise(target: NetworkAPI.get_balance)
                    .then(estimateSubmitHashFunc)
                    .then(estimateRequestSwypeCodeFunc)
                    .done(doneFunc)
                    .catch(catchFunc)
        }
    }

    private func purchaseSwypeCode() {
        proverRotatingView.startAnimation()

        let provider = self.provider

        provider.promise(target: NetworkAPI.request_swype_code)
                .done { [weak self] (result: TxHashResponse) in
                    self?.swypeCodeRequestTxHash = result.txhash
                    self?.switchState()
                }
                .catch { [weak self] (error: Error) in
                    self?.proverRotatingView.stopAnimation()
                    self?.revertState()
                    self?.requestFailed(error)
                }
    }

    private func requestFastSwypeCode() {

        proverRotatingView.startAnimation()

        isSwypeCodeDownloaded = false
        fastSwypeCode = nil

        let provider = self.provider

        provider.promise(target: NetworkAPI.fast_request_swype_code)
                .done { [weak self] (result: FastSwypeCodeRequestResponse) in

                    self?.referenceBlockHeight = result.referenceBlockHeight

                    self?.proverRotatingView.stopAnimation()

                    self?.isSwypeCodeDownloaded = true
                    self?.fastSwypeCode = result.swypeSequence
                    self?.switchState()
                }
                .catch { [weak self] (error: Error) in
                    self?.proverRotatingView.stopAnimation()
                    self?.revertState()
                    self?.requestFailed(error)
                }
    }

    private func downloadSwypeCode() {
        
        isSwypeCodeDownloaded = false
        
        provider.promise(target: NetworkAPI.download_swype_code(txHash: swypeCodeRequestTxHash!))
                .done { [weak self] (result: SwypeCodeRequestResponse) in
                    print("[SwypeViewController] SWYPE CODE = \"\(result.swypeSequence ?? "")\"")

                    guard self?.controllerState == .recording else {
                        return
                    }

                    guard let swypeSequence = result.swypeSequence else {
                        self?.pendingDownloadingSwypeCode = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) { [weak self] in
                            self?.pendingDownloadingSwypeCode = false
                            self?.downloadSwypeCode()
                        }
                        return
                    }

                    self?.proverRotatingView.stopAnimation()
                    self?.isSwypeCodeDownloaded = true
                    
                    let swypeLength = swypeSequence.filter { $0 != "*" }.count
                    
                    self?.videoProcessor?.setSwypeCode(swypeSequence)
                    self?.detectorView.setSwypeLength(to: swypeLength)
                }
                .catch { [weak self] (error: Error) in
                    // The temporary workaround for RS's backend
                    if error is NullResultError {
                        self?.pendingDownloadingSwypeCode = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) { [weak self] in
                            self?.pendingDownloadingSwypeCode = false
                            self?.downloadSwypeCode()
                        }
                        return
                    }

                    let urlErrorCode = (error as? MoyaError)?.urlError?.urlErrorCode

                    guard urlErrorCode == .notConnectedToInternet else {
                        self?.proverRotatingView.stopAnimation()
                        self?.revertState()
                        self?.requestFailed(error)
                        return
                    }

                    if !(self?.isStateSuspended ?? false) {
                        self?.downloadSwypeCode()
                    }
                }
    }

    private func submitMediaHashForFastSwypeCode() {

        proverRotatingView.startAnimation()

        let url = videoProcessor.videoURL!
        let mediaHash = url.sha256().hexDescription
        let entry = NetworkAPI.submit_media_hash_for_fast_swype_request(mediaHash: mediaHash, referenceBlockHeight: referenceBlockHeight!)

        provider.promise(target: entry)
                .done { [weak self] (result: TxHashResponse) in
                    self?.submitMediaHashTxHash = result.txhash
                    self?.switchState()
                }
                .catch { [weak self] (error: Error) in
                    if !(self?.isStateSuspended ?? false) {
                        self?.proverRotatingView.stopAnimation()
                        self?.revertState()
                        self?.requestFailed(error)
                    }
                }
    }

    private func submitMediaHashForNormalSwypeCode() {

        proverRotatingView.startAnimation()

        let url = videoProcessor.videoURL!
        let mediaHash = url.sha256().hexDescription
        let entry = NetworkAPI.submit_media_hash_for_normal_swype_request(mediaHash: mediaHash, referenceTxHash: swypeCodeRequestTxHash!)

        provider.promise(target: entry)
                .done { [weak self] (result: TxHashResponse) in
                    self?.submitMediaHashTxHash = result.txhash
                    self?.switchState()
                }
                .catch { [weak self] (error: Error) in
                    if !(self?.isStateSuspended ?? false) {
                        self?.proverRotatingView.stopAnimation()
                        self?.revertState()
                        self?.requestFailed(error)
                    }
                }
    }

    private func confirmMediaHashSubmission() {

        let confirmEntry = NetworkAPI.confirm_media_hash_submission(txHash: submitMediaHashTxHash!)

        provider.promise(target: confirmEntry)
                .done { [weak self] (result: ConfirmSubmissionResponse) in
                    guard result.height != nil else {
                        self?.pendingConfirmingVideoHashSubmission = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) { [weak self] in
                            self?.pendingConfirmingVideoHashSubmission = false
                            self?.confirmMediaHashSubmission()
                        }
                        return
                    }

                    self?.proverRotatingView.stopAnimation()
                    self?.switchState()
                }
                .catch { [weak self] (error: Error) in
                    let urlErrorCode = (error as? MoyaError)?.urlError?.urlErrorCode

                    guard urlErrorCode == .notConnectedToInternet else {
                        self?.proverRotatingView.stopAnimation()
                        self?.revertState()
                        self?.requestFailed(error)
                        return
                    }

                    if !(self?.isStateSuspended ?? false) {
                        self?.confirmMediaHashSubmission()
                    }
                }
    }

    func warnAboutSwypeCodeNotEntered(send: @escaping () -> Void, discard: @escaping () -> Void) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .alert)
        
        alertController.setValue(swypeCodeBodyMessage(), forKey: "attributedMessage")
        
        let cancelAction = UIAlertAction(title: localize("swype_alert_cancel"), style: .cancel) { _ in }
        let sendAction = UIAlertAction(title: localize("swype_alert_send"), style: .default) { _ in send() }
        let discardAction = UIAlertAction(title: localize("swype_alert_discard"), style: .default) { _ in discard() }
        
        alertController.addAction(cancelAction)
        alertController.addAction(sendAction)
        alertController.addAction(discardAction)
        
        present(alertController, animated: true, completion: nil)
    }

    func swypeCodeBodyMessage() -> NSAttributedString {
        let titleFont = UIFont.boldSystemFont(ofSize: 20)
        let mediumFont = UIFont.systemFont(ofSize: 17)
        let boldFont = UIFont.boldSystemFont(ofSize: 17)
        
        let res = NSMutableAttributedString(string: localize("swype_code_res"), attributes: [.font: titleFont])
        var body = ""
        
        body.append(localize("swype_code_body_1"))
        body.append(localize("swype_code_body_2"))
        body.append(localize("swype_code_body_3"))
        
        res.append(body.simpleFormatting(mediumFont: mediumFont, boldFont: boldFont))
        
        return res
    }
}

// MARK: - Private methods
private extension SwypeViewController {

    #if arch(x86_64) || arch(i386)
    func skipSwypeViewController(message: String) { }
    #else
    func skipSwypeViewController(message: String) {

        let alertController = UIAlertController(
                title: "Please Allow Access",
                message: message,
                preferredStyle: .alert)

        let notNowAction = UIAlertAction(title: "Not Now", style: .cancel) { _ in
            let vc = UserPagesViewController()
            self.show(vc, sender: nil)
        }

        let switchToSettingsAction = UIAlertAction(title: "Settings", style: .default) { _ in
            if let settingsUrl = URL(string: UIApplication.openSettingsURLString) {
                UIApplication.shared.open(settingsUrl)
            }
        }

        alertController.addAction(notNowAction)
        alertController.addAction(switchToSettingsAction)

        present(alertController, animated: true, completion: nil)
    }
    #endif
}

extension SwypeViewController: VideoProcessorDelegate {
    private func showAlert(text: String) {
        let alertController = UIAlertController(title: nil, message: text, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: localize("swype_alert_ok"), style: .cancel, handler: nil))
        present(alertController, animated: true, completion: nil)
    }
    
    func recordFailed() {
        print("[SwypeViewController] recordFailed()")
        
        DispatchQueue.main.async { [weak self] in
            self?.switchState()
        }
    }
    
    func recordFinished() {
        print("[SwypeViewController] recordFinished()")
    }
    
    func metadataSaved(status: AVAssetExportSession.Status) {
        print("[SwypeViewController] metadataSaved()")
        
        //DispatchQueue.main.async {
        //    SwypeViewController.attemptRotationToDeviceOrientation()
        //}
        
        DispatchQueue.main.async { [weak self] in
            if status == .completed {
                self?.saveVideo()
            }
            
            self?.switchState()
        }
    }
    
    private func saveVideo() {
        PHPhotoLibrary.requestAuthorization { [weak self] (status) in
            switch status {
            case .authorized:
                guard let `self` = self else {
                    return
                }
                
                let url = self.videoProcessor.videoURL!
                //self.showAlert(text: "Video will be saved to camera roll.")
                UISaveVideoAtPathToSavedPhotosAlbum(
                    url.relativePath,
                    self,
                    #selector(self.onVideoSaved),
                    nil)
            case .denied, .restricted:
                //self.showAlert(text:
                //  "Video will be saved to documents folder. If you want to " +
                //  "save video to camera roll allow access to Photos in Settings")
                //self.saveToDocuments(from: url, withExtension: "mov")
                break
            case .notDetermined:
                fatalError("[VideoDetector] authorization status: \(status)")
            }
        }
    }
    
    @objc private func onVideoSaved(urlPath: String?, didFinishSavingWithError error: Error?, contextInfo context: Any?)  {
        if let error = error {
            fatalError("[VideoDetector] onVideoSaved error saving '\(urlPath!)' = \(error.localizedDescription)")
        }
    }
    
    private func saveToDocuments(from url: URL, withExtension fileExtension: String) {
        
        guard let data = try? Data(contentsOf: url) else { return }
        
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        let name = dateFormatter.string(from: Date())
        
        let urlToSave = FileManager.documentURL.appendingPathComponent("\(name).\(fileExtension)")
        try? data.write(to: urlToSave)
    }
    
    func recorderDidUpdateFps(_ fps: Double) {
        videoCaptureFps = fps
        updateFpsLabel()
    }
    
    func detectorDidUpdateFps(_ fps: Double) {
        videoDetectionFps = fps
    }
    
    private func updateFpsLabel() {
        DispatchQueue.main.async { [weak self] in
            if let `self` = self {
                self.fpsLabel.text = String(format: "%.1f/%.1f %@", self.videoCaptureFps, self.videoDetectionFps, localize("swype_fps"))
            }
        }
    }
}

// MARK: - State machine
extension SwypeViewController {
    
    func updateState(updateInfoViews: Bool = true) {
        
        if !Thread.isMainThread {
            DispatchQueue.main.async { [weak self] in
                self?.updateState(updateInfoViews: updateInfoViews)
            }
            return
        }
        
        if updateInfoViews {
            detectorView.update(from: prevControllerState, to: controllerState, isNotEnoughFunds: isNotEnoughFunds())
            
            balanceLabel.update(balance: balance)
            priceLabelContainer.update(to: controllerState, isNotEnoughFunds: isNotEnoughFunds())
            priceLabel.updateState(to: controllerState, price: price)
        }
        
        switch controllerState {
        case .readyToPurchaseSwypeCode,
             .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenPurchase,
             .purchasingSwypeCode,
             .finishingRecord,
             .submittingVideoHash,
             .confirmingVideoHashSubmission:
            recordButton.setImage(image: #imageLiteral(resourceName: "start_record"))
        case .recording:
            recordButton.setImage(image: #imageLiteral(resourceName: "stop_record"))
        }
        
        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenPurchase,
             .purchasingSwypeCode,
             .finishingRecord,
             .submittingVideoHash,
             .confirmingVideoHashSubmission:
            recordButton.isEnabled = false
        case .readyToPurchaseSwypeCode,
             .recording:
            recordButton.isEnabled = true
        }
        
        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .readyToPurchaseSwypeCode:
            walletButton.isHidden = false
            settingsButton.isHidden = false
            guideButton.isHidden = false
        default:
            walletButton.isHidden = true
            settingsButton.isHidden = true
            guideButton.isHidden = true
        }
        
        walletButton.isEnabled = true
        settingsButton.isEnabled = true
        guideButton.isEnabled = true
    }
}

// MARK: - ROUTING
extension SwypeViewController {
    func routeToWallet() {
        isNavigatingToSettings = true
        
        let userPagesViewController = UserPagesViewController()
        show(userPagesViewController, sender: nil)
    }
    
    func routeToSettings() {
        isNavigatingToSettings = true
        
        let userPagesViewController = UserPagesViewController()
        userPagesViewController.openSettings = true
        show(userPagesViewController, sender: nil)
    }
    
    func routeToGuide() {
        let guideViewController = UserGuideViewController()
        show(guideViewController, sender: nil)
    }
}
