import Foundation

extension FileManager {
    static var documentURL: URL {
        let url = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        return url
    }

    static var tempURL: URL {
        return FileManager.default.temporaryDirectory
    }

    static func clearDocumentsDirectory() {
        clearDirectory(documentURL)
    }

    static func clearTempDirectory() {
        clearDirectory(tempURL)
    }

    private static func clearDirectory(_ url: URL) {
        let instance = self.default
        let contents = try! instance.contentsOfDirectory(atPath: url.path)

        do {
            try contents.forEach { [unowned instance] basename in
                let filePath = url.appendingPathComponent(basename, isDirectory: false).path
                try instance.removeItem(atPath: filePath)
            }
        }
        catch {
            let err = error.localizedDescription
            fatalError("[FileManager] Could not clear directory: \(err)")
        }
    }

    static func loopOverContentsOfDocuments() {
        loopOverContents(documentURL)
    }

    static func loopOverContentsOfTemp() {
        loopOverContents(tempURL)
    }

    private static func loopOverContents(_ url: URL) {
        let instance = self.default
        let contents = try! instance.contentsOfDirectory(atPath: url.path)

        contents.forEach { basename in
            var filePath = url.appendingPathComponent(basename, isDirectory: false).path
            var attr = try! instance.attributesOfItem(atPath: filePath)
            var size = attr[.size] as! UInt64

            size = 0
        }
    }
}
