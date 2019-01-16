import Foundation

extension URL {

    func sha256() -> Data {
        let bufferSize = 1024 * 1024
        let resources = try! resourceValues(forKeys:[.fileSizeKey])
        let fileSize = resources.fileSize!
        var context = CC_SHA256_CTX()
        let file = try! FileHandle(forReadingFrom: self)

        CC_SHA256_Init(&context)

        defer {
            file.closeFile()
        }

        debugPrint("File size is", fileSize)

        let start = Date()

        debugPrint("Start time is", start)

        while autoreleasepool(invoking: {
            let data = file.readData(ofLength: bufferSize)
            let notEmpty = data.count > 0

            if notEmpty {
                data.withUnsafeBytes {
                    _ = CC_SHA256_Update(&context, $0, numericCast(data.count))
                }
            }

            return notEmpty
        }) { }

        let time = Date().timeIntervalSince(start)

        debugPrint("Calculating file of size", fileSize, "took", time, "seconds")
        debugPrint("Speed is \(Double(fileSize) / time)")

        var digest = Data(count: Int(CC_SHA256_DIGEST_LENGTH))

        digest.withUnsafeMutableBytes {
            _ = CC_SHA256_Final($0, &context)
        }

        debugPrint("SHA256 is \(digest.hexDescription)")
        
        return digest
    }
}
