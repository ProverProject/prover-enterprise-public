import Foundation

public extension FileManager {
    static var documentURL: URL {
        let url = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        return url
    }

    static var inboxURL: URL {
        return documentURL.appendingPathComponent("Inbox", isDirectory: true)
    }
    
    static var tempURL: URL {
        return FileManager.default.temporaryDirectory
    }

    static func contentsOfDocumentsDirectory(exts: [String]? = nil) -> [URL] {
        return contentsOfDirectory(documentURL, exts: exts)
    }
    
    static func contentsOfInboxDirectory(exts: [String]? = nil) -> [URL] {
        return contentsOfDirectory(inboxURL, exts: exts)
    }
    
    static func contentsOfTempDirectory(exts: [String]? = nil) -> [URL] {
        return contentsOfDirectory(tempURL, exts: exts)
    }
    
    static func clearDocumentsDirectory(exts: [String]? = nil) {
        clearDirectory(documentURL, exts: exts)
    }

    static func clearInboxDirectory(exts: [String]? = nil) {
        clearDirectory(inboxURL, exts: exts)
    }
    
    static func clearTempDirectory(exts: [String]? = nil) {
        clearDirectory(tempURL, exts: exts)
    }

    private static func contentsOfDirectory(_ url: URL, exts: [String]?) -> [URL] {
        guard var basenames = try? FileManager.default.contentsOfDirectory(atPath: url.path) else {
            return []
        }
        
        if let exts = exts {
            basenames = basenames.filter { basename in
                exts.first { ext in basename.hasSuffix(ext) } != nil
            }
        }
        
        return basenames.map { url.appendingPathComponent($0, isDirectory: false) }
    }

    private static func clearDirectory(_ url: URL, exts: [String]?) {
        let fileURLs = contentsOfDirectory(url, exts: exts)

        do {
            try fileURLs.forEach { fileURL in
                try FileManager.default.removeItem(at: fileURL)
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
