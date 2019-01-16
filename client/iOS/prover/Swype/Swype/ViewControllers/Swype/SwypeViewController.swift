import UIKit
import AVFoundation
import Accelerate
import Photos
import PromiseKit
import Moya
import Common

protocol VideoProcessorDelegate: class {
    var controllerState: SwypeViewControllerState { get }
    func detectorDidChangeControllerState(to state: SwypeViewControllerState)
    func detectorDidChangeSwypeIndex(to index: Int)
    func recordFailed()
    func recordFinished()
    func metadataSaved(status: AVAssetExportSession.Status)
    func recorderDidUpdateFps(_ fps: Double)
    func detectorDidUpdateFps(_ fps: Double)
}

class SwypeViewController: BaseViewController, SuspendableStateMachine {

    // MARK: - IBOutlet
    @IBOutlet weak var detectorView: SwypeDetectorView!
    @IBOutlet weak var infoView: InfoView!
    @IBOutlet weak var progressSwype: UIPageControl!
    @IBOutlet weak var videoPreviewView: VideoPreviewView!
    @IBOutlet weak var recordButton: UIButton!
    @IBOutlet weak var walletButton: UIButton!
    @IBOutlet weak var settingsButton: UIButton!
    @IBOutlet weak var guideButton: UIButton!

    @IBOutlet weak var balanceLabel: BalanceLabel!
    @IBOutlet weak var proverRotatingView: SpinnerView!
    @IBOutlet weak var priceLabelContainer: PriceLabelContainer!
    @IBOutlet weak var priceLabel: PriceLabel!

    @IBOutlet weak var fpsLabel: UILabel!

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

    private var pendingDownloadingSwypeCode = false
    private var pendingConfirmingMediaHashSubmission = false
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

    // To be called by self.reachability, see its type declaration for details
    func suspendState() {

        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .waitingForCode,
             .submittingMediaHash,
             .confirmingMediaHashSubmission:
            isStateSuspended = true

        case .readyToGetSwypeCode,
             .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .didReceiveCode,
             .waitingForCircle,
             .waitingToStartSwypeCode,
             .detectingSwypeCode,
             .finishingWithoutDetectedSwypeCode,
             .swypeCodeDetected,
             .finishingWithDetectedSwypeCode:
            isStateSuspended = false
        }
    }

    // To be called by self.reachability, see its type declaration for details
    func resumeState() {

        // If we are in .readyToGetSwypeCode then we anyway refresh the balance
        guard isStateSuspended || controllerState == .readyToGetSwypeCode else {
            return
        }

        isStateSuspended = false

        switch controllerState {
        case .readyToGetSwypeCode:
            controllerState = .requestingBalanceAndPriceOnAppearance
            requestBalanceAndPrice()

        case .requestingBalanceAndPriceOnAppearance:
            requestBalanceAndPrice()

        case .waitingForCode:
            if Settings.useFastSwypeCode {
                requestFastCode()
            }
            else {
                if !provider.isRequesting && !pendingDownloadingSwypeCode {
                    downloadCode()
                }
            }

        case .submittingMediaHash:
            if Settings.useFastSwypeCode {
                submitMediaHashForFastSwypeCode()
            }
            else {
                submitMediaHashForNormalSwypeCode()
            }

        case .confirmingMediaHashSubmission:
            if !provider.isRequesting && !pendingConfirmingMediaHashSubmission {
                confirmMediaHashSubmission()
            }

        case .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .didReceiveCode,
             .waitingForCircle,
             .waitingToStartSwypeCode,
             .detectingSwypeCode,
             .finishingWithoutDetectedSwypeCode,
             .swypeCodeDetected,
             .finishingWithDetectedSwypeCode:
            break
        }
    }

    private func revertState() {
        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .submittingMediaHash,
             .confirmingMediaHashSubmission:
            controllerState = .readyToGetSwypeCode

        case .readyToGetSwypeCode,
             .waitingForCode,
             .didReceiveCode,
             .waitingForCircle,
             .waitingToStartSwypeCode,
             .detectingSwypeCode,
             .finishingWithoutDetectedSwypeCode,
             .swypeCodeDetected,
             .finishingWithDetectedSwypeCode:
            break
        }
    }

    private func switchState() {
        
        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance:
            controllerState = .readyToGetSwypeCode

        case .readyToGetSwypeCode:
            if Settings.useFastSwypeCode {
                controllerState = .waitingForCode
                requestFastCode()
                videoProcessor.startRecord()
            }
            else {
                controllerState = .requestingBalanceAndPriceThenGetSwypeCode
                requestBalanceAndPrice()
            }

        case .requestingBalanceAndPriceThenGetSwypeCode:
            controllerState = .gettingSwypeCode
            getSwypeCode()

        case .gettingSwypeCode:
            controllerState = .waitingForCode
            downloadCode()
            videoProcessor.startRecord()

        case .waitingForCode,
             .didReceiveCode,
             .waitingForCircle,
             .waitingToStartSwypeCode,
             .detectingSwypeCode:
            controllerState = .finishingWithoutDetectedSwypeCode
            videoProcessor.stopRecord()

        case .finishingWithoutDetectedSwypeCode:
            controllerState = .requestingBalanceAndPriceOnAppearance
            requestBalanceAndPrice()

        case .swypeCodeDetected:
            controllerState = .finishingWithDetectedSwypeCode
            videoProcessor.stopRecord()

        case .finishingWithDetectedSwypeCode:
            controllerState = .submittingMediaHash
            if Settings.useFastSwypeCode {
                submitMediaHashForFastSwypeCode()
            }
            else {
                submitMediaHashForNormalSwypeCode()
            }

        case .submittingMediaHash:
            controllerState = .confirmingMediaHashSubmission
            confirmMediaHashSubmission()

        case .confirmingMediaHashSubmission:
            controllerState = .requestingBalanceAndPriceOnAppearance
            requestBalanceAndPrice()
        }
    }

    @IBAction func walletButtonAction(_ sender: UIButton) {
        let vc = PurchasesViewController()
        show(vc, sender: nil)
    }

    @IBAction func settingsButtonAction(_ sender: UIButton) {
        let purchasesViewController = PurchasesViewController()
        purchasesViewController.openSettings = true
        show(purchasesViewController, sender: nil)
    }

    @IBAction func guideButtonAction(_ sender: UIButton) {
        let guideViewController = UserGuideViewController()
        show(guideViewController, sender: nil)
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

    public var controllerState: SwypeViewControllerState = .readyToGetSwypeCode {
        didSet {
            updateState(updateInfoViews: true)
        }
    }

    private var videoProcessorDidLayoutSubviews: Bool = false
    
    // MARK: - View controller lifecycle
    override func viewDidLoad() {
        super.viewDidLoad()

        reachability = NetworkReachability(stateMachine: self)

    #if DEBUG
        infoView.isHidden = false
    #else
        infoView.isHidden = true
    #endif
    }

    override func viewWillAppear(_ animated: Bool) {
        print("[SwypeViewController] viewWillAppear")
        super.viewWillAppear(animated)

        addBackgroundNotificationObservers()

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

        videoProcessor = VideoProcessor(
                videoPreviewView: videoPreviewView,
                coordinateDelegate: detectorView,
                delegate: self)
        videoProcessor.startCapture()

        switch controllerState {
        case .readyToGetSwypeCode:
            controllerState = .requestingBalanceAndPriceOnAppearance
            requestBalanceAndPrice()

        case .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .waitingForCode,
             .submittingMediaHash,
             .confirmingMediaHashSubmission:
            proverRotatingView.startAnimation()
            
        case .didReceiveCode,
             .waitingForCircle,
             .waitingToStartSwypeCode,
             .detectingSwypeCode,
             .finishingWithoutDetectedSwypeCode,
             .swypeCodeDetected,
             .finishingWithDetectedSwypeCode:
            break
        }
    }

    override func viewDidDisappear(_ animated: Bool) {
        print("[SwypeViewController] viewDidDisappear")
        super.viewDidDisappear(animated)

        if videoProcessor != nil {
            videoProcessor.stopCapture()
            videoProcessor = nil
        }

        removeBackgroundNotificationObservers()
    }

    override var preferredStatusBarStyle : UIStatusBarStyle {
        return .lightContent
    }

    @objc func handleWillEnterForeground() {

        print("[SwypeViewController] handleWillEnterForeground()")

        videoProcessor.startCapture()
    }

    @objc func handleDidEnterBackground() {
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

    private func getSwypeCode() {
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

    private func requestFastCode() {

        proverRotatingView.startAnimation()

        let provider = self.provider

        provider.promise(target: NetworkAPI.fast_request_swype_code)
                .done { [weak self] (result: FastSwypeCodeRequestResponse) in

                    self?.referenceBlockHeight = result.referenceBlockHeight

                    self?.proverRotatingView.stopAnimation()
                    self?.controllerState = .didReceiveCode

                    let swypeLength = result.swypeSequence.filter { $0 != "*" }.count

                    self?.videoProcessor?.setSwypeCode(result.swypeSequence)
                    self?.progressSwype.setNumberOfPages(swypeLength)
                }
                .catch { [weak self] (error: Error) in
                    self?.proverRotatingView.stopAnimation()
                    self?.revertState()
                    self?.requestFailed(error)
                }
    }

    private func downloadCode() {

        let provider = self.provider

        provider.promise(target: NetworkAPI.download_swype_code(txHash: swypeCodeRequestTxHash!))
                .done { [weak self] (result: SwypeCodeRequestResponse) in
                    print("[SwypeViewController] SWYPE CODE = \"\(result.swypeSequence ?? "")\"")

                    guard self?.controllerState == .waitingForCode else {
                        return
                    }

                    guard let swypeSequence = result.swypeSequence else {
                        self?.pendingDownloadingSwypeCode = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) { [weak self] in
                            self?.pendingDownloadingSwypeCode = false
                            self?.downloadCode()
                        }
                        return
                    }

                    self?.proverRotatingView.stopAnimation()
                    self?.controllerState = .didReceiveCode

                    let swypeLength = swypeSequence.filter { $0 != "*" }.count

                    self?.videoProcessor?.setSwypeCode(swypeSequence)
                    self?.progressSwype.setNumberOfPages(swypeLength)
                }
                .catch { [weak self] (error: Error) in
                    // The temporary workaround for RS's backend
                    if error is NullResultError {
                        self?.pendingDownloadingSwypeCode = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) { [weak self] in
                            self?.pendingDownloadingSwypeCode = false
                            self?.downloadCode()
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
                        self?.downloadCode()
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
                        self?.pendingConfirmingMediaHashSubmission = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) { [weak self] in
                            self?.pendingConfirmingMediaHashSubmission = false
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
            let vc = PurchasesViewController()
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

// MARK: - Notification
private extension SwypeViewController {
    
    func addBackgroundNotificationObservers() {
        
        print("[SwypeViewController] addBackgroundNotificationObservers()")

        notificationCenter.addObserver(
                self,
                selector: #selector(handleWillEnterForeground),
                name: UIApplication.willEnterForegroundNotification,
                object: nil)

        notificationCenter.addObserver(
                self,
                selector: #selector(handleDidEnterBackground),
                name: UIApplication.didEnterBackgroundNotification,
                object: nil)
    }
    
    func removeBackgroundNotificationObservers() {
        
        print("[SwypeViewController] removeBackgroundNotificationObservers()")

        notificationCenter.removeObserver(self, name: UIApplication.didEnterBackgroundNotification, object: nil)
        notificationCenter.removeObserver(self, name: UIApplication.willEnterForegroundNotification, object: nil)
    }
}

extension SwypeViewController: VideoProcessorDelegate {
    private func showAlert(text: String) {
        let alertController = UIAlertController(title: nil, message: text, preferredStyle: .alert)
        alertController.addAction(UIAlertAction(title: "OK", style: .cancel, handler: nil))
        present(alertController, animated: true, completion: nil)
    }
    
    func detectorDidChangeControllerState(to state: SwypeViewControllerState) {
        DispatchQueue.main.async { [unowned self] in
            self.controllerState = state
        }
    }

    func detectorDidChangeSwypeIndex(to index: Int) {
        DispatchQueue.main.async { [unowned self] in
            self.progressSwype.setCurrentStep(index - 2)
        }
    }

    func recordFailed() {
        print("[SwypeViewController] recordFailed()")

        DispatchQueue.main.async { [unowned self] in
            self.switchState()
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

        DispatchQueue.main.async { [unowned self] in
            if status == .completed {
                self.saveVideo()
            }

            self.switchState()
        }
    }

    private func saveVideo() {
        PHPhotoLibrary.requestAuthorization { [unowned self] (status) in
            switch status {
            case .authorized:
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
        DispatchQueue.main.async { [unowned self] in
            self.fpsLabel.text = String(format: "%.1f/%.1f fps", self.videoCaptureFps, self.videoDetectionFps)
            //self.fpsLabel.text = String(format: "%f/%f fps", self.videoCaptureFps, self.videoDetectionFps)
        }
    }
}

// MARK: - State machine
extension SwypeViewController {

    func updateState(updateInfoViews: Bool) {

        if !Thread.isMainThread {
            DispatchQueue.main.async { [unowned self] in
                self.updateState(updateInfoViews: updateInfoViews)
            }
            return
        }

        if updateInfoViews {
            progressSwype.update(by: controllerState)
            infoView.update(by: controllerState, isNotEnoughFunds: isNotEnoughFunds())
            detectorView.update(by: controllerState)

            balanceLabel.update(balance: balance)
            priceLabelContainer.update(by: controllerState, isNotEnoughFunds: isNotEnoughFunds())
            priceLabel.updateState(by: controllerState, price: price)
        }
        
        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .readyToGetSwypeCode,
             .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .finishingWithoutDetectedSwypeCode,
             .finishingWithDetectedSwypeCode,
             .submittingMediaHash,
             .confirmingMediaHashSubmission:
            recordButton.setImage(image: #imageLiteral(resourceName: "start_record"))
        case .waitingForCode,
             .didReceiveCode,
             .waitingForCircle,
             .waitingToStartSwypeCode,
             .detectingSwypeCode,
             .swypeCodeDetected:
            recordButton.setImage(image: #imageLiteral(resourceName: "stop_record"))
        }

        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .requestingBalanceAndPriceThenGetSwypeCode,
             .gettingSwypeCode,
             .finishingWithoutDetectedSwypeCode,
             .finishingWithDetectedSwypeCode,
             .submittingMediaHash,
             .confirmingMediaHashSubmission:
            recordButton.isEnabled = false
        case .readyToGetSwypeCode,
             .waitingForCode,
             .didReceiveCode,
             .waitingForCircle,
             .waitingToStartSwypeCode,
             .detectingSwypeCode,
             .swypeCodeDetected:
            recordButton.isEnabled = true
        }

        switch controllerState {
        case .requestingBalanceAndPriceOnAppearance,
             .readyToGetSwypeCode:
            walletButton.isEnabled = true
            settingsButton.isEnabled = true
            guideButton.isEnabled = true
        default:
            walletButton.isEnabled = false
            settingsButton.isEnabled = false
            guideButton.isEnabled = false
        }
    }
}
