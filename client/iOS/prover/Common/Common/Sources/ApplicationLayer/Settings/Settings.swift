import Foundation
import AVFoundation

public class Settings {
    
    public static var isFirstLaunch: Bool {
        get {
            return UserDefaults.standard.object(forKey: #function) as? Bool ?? true
        }
        set {
            UserDefaults.standard.set(newValue, forKey: #function)
        }
    }

    public static var baseURL: String? {
        get {
            return UserDefaults.standard.string(forKey: #function)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: #function)
        }
    }

    public static var allowUserChooseSwypeType: Bool {
        return true
    }

    public static var useFastSwypeCode: Bool {
        get {
            return UserDefaults.standard.bool(forKey: #function)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: #function)
        }
    }

    public static var isBaseURLValidated: Bool {
        get {
            return UserDefaults.standard.bool(forKey: #function)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: #function)
        }
    }

    public static var qrMessage: String? {
        get {
            return UserDefaults.standard.string(forKey: #function)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: #function)
        }
    }

    public static var qrCodeData: Data? {
        get {
            return UserDefaults.standard.data(forKey: #function)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: #function)
        }
    }

    public static func availableVideoOutputPresets() -> [AVOutputSettingsPreset] {
        return AVOutputSettingsAssistant.availableOutputSettingsPresets()
                                        .filter {
                                            videoOutputResolution(forPreset: $0).width <= 1920
                                        }
    }

    private static var defaultVideoOutputPreset: AVOutputSettingsPreset {
        let availableOutputPresets = availableVideoOutputPresets()

        guard availableOutputPresets.contains(.preset1280x720) else {
            return availableOutputPresets.first!
        }

        return .preset1280x720
    }

    public static var currentVideoOutputPreset: AVOutputSettingsPreset {
        get {
            guard let presetRawValue = UserDefaults.standard.string(forKey: #function) else {
                return defaultVideoOutputPreset
            }
            return AVOutputSettingsPreset(rawValue: presetRawValue)
        }
        set {
            UserDefaults.standard.setValue(newValue.rawValue, forKey: #function)
        }
    }

    public static var currentVideoCapturePreset: AVCaptureSession.Preset {
        let preset = currentVideoOutputPreset

        if #available(iOS 11.0, *) {
            if preset.rawValue.contains("HEVC") {
                switch preset {
                case .hevc1920x1080:
                    return .hd1920x1080
                case .hevc3840x2160:
                    return .hd4K3840x2160
                default:
                    fatalError("[Settings] Unrecognized video capture HEVC preset!")
                }
            }
        }

        switch preset {
        case .preset640x480:
            return .vga640x480
        case .preset960x540:
            return .iFrame960x540
        case .preset1280x720:
            return .hd1280x720
        case .preset1920x1080:
            return .hd1920x1080
        case .preset3840x2160:
            return .hd4K3840x2160
        default:
            fatalError("[Settings] Unrecognized video capture preset!")
        }
    }

    public static var currentVideoOutputResolution: (width: UInt, height: UInt) {
        return videoOutputResolution(forPreset: currentVideoOutputPreset)
    }

    public static func videoOutputResolution(forPreset preset: AVOutputSettingsPreset) -> (width: UInt, height: UInt) {
        let assistant = AVOutputSettingsAssistant(preset: preset)
        let settings = assistant!.videoSettings!
        let settingsWidth = settings[AVVideoWidthKey] as! UInt
        let settingsHeight = settings[AVVideoHeightKey] as! UInt

        return (settingsWidth, settingsHeight)
    }

    public static var showFps: Bool {
        get {
            return UserDefaults.standard.bool(forKey: #function)
        }
        set {
            UserDefaults.standard.set(newValue, forKey: #function)
        }
    }
}
