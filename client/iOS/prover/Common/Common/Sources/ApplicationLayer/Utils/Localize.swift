public func localize(_ key: String) -> String {
    return NSLocalizedString(key, comment: "")
}

public class Utils {
    public class func localizeSelf(_ key: String) -> String {
        return NSLocalizedString(key, tableName: nil, bundle: Bundle(for: self), value: "", comment: "")
    }
}
