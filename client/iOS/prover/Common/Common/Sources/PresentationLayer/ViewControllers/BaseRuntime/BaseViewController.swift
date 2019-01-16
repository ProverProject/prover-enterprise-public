import UIKit
import Moya
import Result
import PromiseKit
import AVFoundation
import Photos

open class BaseViewController: UIViewController, AlertingViewController {
    public var app: UIApplication {
        return UIApplication.shared
    }

    public var appWindow: UIWindow {
        return UIApplication.shared.delegate!.window!!
    }

    public var notificationCenter: NotificationCenter {
        return NotificationCenter.default
    }

    public var isWaiting: Bool = false {
        didSet {
            if isWaiting {
                view.addWaiterView()
            } else {
                view.removeWaiterView()
            }
        }
    }

    public lazy var provider = NetworkProvider()

    public func alert(_ message: String) {
        let alertController = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        let okAction = UIAlertAction(title: "OK", style: .cancel)

        alertController.addAction(okAction)
        present(alertController, animated: true)
    }

    public func requestFailed(_ error: Error) {

        guard let moyaError = error as? MoyaError else {
            print("[BaseViewController] requestFailed(): error.localizedDescription = \(error.localizedDescription)")
            alert(error.localizedDescription)
            return
        }

        //let moyaError = error as! MoyaError

        print("[BaseViewController] requestFailed(): moyaError.errorDescription! = \(moyaError.errorDescription!)")

        let urlError = moyaError.urlError
        let urlErrorCode = urlError?.urlErrorCode

        if let urlError = urlError {
            print("[BaseViewController] requestFailed(): urlError errorCode     = \(urlError.errorCode)")
            print("[BaseViewController] requestFailed(): urlError urlErrorCode  = \(urlError.urlErrorCode)")
            print("[BaseViewController] requestFailed(): urlError localDesc     = \(urlError.localizedDescription)")
        }

        if let response = moyaError.response {
            print("[BaseViewController] requestFailed(): response.statusCode = \(response.statusCode)")

            let strResponse = String(data: response.data, encoding: .utf8)!
            print("[BaseViewController] requestFailed(): error response = \(strResponse)")

        } else {
            print("[BaseViewController] requestFailed(): response = nil")
        }

        alert(moyaError.errorDescription!)

        //if !(urlErrorCode == .notConnectedToInternet || urlErrorCode == .cancelled) {
        //    alert(moyaError.errorDescription!)
        //}
    }

    open override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        if SharedSettings.shared.shouldAskForAccessToCamera {
            AVCaptureDevice.requestAccess(for: .video) { _ in }
        }

        if SharedSettings.shared.shouldAskForAccessToMicrophone {
            AVCaptureDevice.requestAccess(for: .audio) { _ in }
        }

        if SharedSettings.shared.shouldAskForAccessToPhotosGallery {
            PHPhotoLibrary.requestAuthorization { _ in }
        }
    }
}
