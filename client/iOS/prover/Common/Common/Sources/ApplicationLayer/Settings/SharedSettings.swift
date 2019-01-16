import UIKit

public enum Version {
    case old, new
}

open class SharedSettings {
    public static let shared = SharedSettings()
    
    public var iosApp: String = ""
    public var guidePages: [GuidePage] = []
    
    public var appVersion: Version = .old

    public var shouldAskForAccessToCamera: Bool = false
    public var shouldAskForAccessToMicrophone: Bool = false
    public var shouldAskForAccessToPhotosGallery: Bool = false
}
