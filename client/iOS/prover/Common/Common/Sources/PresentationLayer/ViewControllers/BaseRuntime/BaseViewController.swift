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

    public var appDelegate: BaseAppDelegate {
        return UIApplication.shared.delegate as! BaseAppDelegate
    }
    
    public var appWindow: UIWindow {
        return UIApplication.shared.delegate!.window!!
    }

    private var notificationCenter: NotificationCenter {
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

    public var topAnchor: NSLayoutYAxisAnchor {
        if #available(iOS 11.0, *) {
            return view.safeAreaLayoutGuide.topAnchor
        } else {
            return topLayoutGuide.bottomAnchor
        }
    }

    public var bottomAnchor: NSLayoutYAxisAnchor {
        if #available(iOS 11.0, *) {
            return view.safeAreaLayoutGuide.bottomAnchor
        } else {
            return bottomLayoutGuide.topAnchor
        }
    }

    public lazy var provider = NetworkProvider()

    public func alert(_ message: String) {
        let alertController = UIAlertController(title: nil, message: message, preferredStyle: .alert)
        let okAction = UIAlertAction(title: Utils.localizeSelf("alert_ok"), style: .cancel)

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

        let moyaErrorDescription = moyaError.errorDescription!

        print("[BaseViewController] requestFailed(): moyaError.errorDescription! = \(moyaErrorDescription)")

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

        if moyaErrorDescription.hasPrefix("wron"), moyaErrorDescription.hasSuffix("session_key") {
            appDelegate.setAuthRootViewController(animated: true)
        } else {
            alert(moyaErrorDescription)
        }

        //if !(urlErrorCode == .notConnectedToInternet || urlErrorCode == .cancelled) {
        //    alert(moyaError.errorDescription!)
        //}
    }

    open override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)

        if SharedSettings.shared.shouldAskForAccessToCamera {
            AVCaptureDevice.requestAccess(for: .video) { _ in
            }
        }

        if SharedSettings.shared.shouldAskForAccessToMicrophone {
            AVCaptureDevice.requestAccess(for: .audio) { _ in
            }
        }

        if SharedSettings.shared.shouldAskForAccessToPhotosGallery {
            PHPhotoLibrary.requestAuthorization { _ in
            }
        }
    }

    open override func viewDidLoad() {
        super.viewDidLoad()
        addBackgroundNotificationObservers()
    }

    deinit {
        removeBackgroundNotificationObservers()
    }
}

extension BaseViewController {

    @objc open func handleWillEnterForeground() { }
    @objc open func handleDidEnterBackground() { }
    @objc open func keyboardWillShow(notification: NSNotification) { }
    @objc open func keyboardWillHide(notification: NSNotification) { }

    private func addBackgroundNotificationObservers() {
        
        print("[BaseViewController] addBackgroundNotificationObservers()")
        
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
        
        notificationCenter.addObserver(
            self,
            selector: #selector(keyboardWillShow),
            name: UIResponder.keyboardWillShowNotification,
            object: nil)
        
        notificationCenter.addObserver(
            self,
            selector: #selector(keyboardWillHide),
            name: UIResponder.keyboardWillHideNotification,
            object: nil)
    }
    
    private func removeBackgroundNotificationObservers() {
        
        print("[BaseViewController] removeBackgroundNotificationObservers()")
        
        notificationCenter.removeObserver(self, name: UIApplication.willEnterForegroundNotification, object: nil)
        notificationCenter.removeObserver(self, name: UIApplication.didEnterBackgroundNotification, object: nil)
        notificationCenter.removeObserver(self, name: UIResponder.keyboardWillShowNotification, object: nil)
        notificationCenter.removeObserver(self, name: UIResponder.keyboardWillHideNotification, object: nil)
    }
}
