//
//  QRGeneratorService.swift
//  Clapperboard
//
//  Created by Александр Соломатов on 12.09.2018.
//  Copyright © 2018 Nordavind. All rights reserved.
//

import UIKit
import BigInt

public extension String {
    var hex2Bytes: [UInt8] {
        let hex = Array(self)
        return stride(from: 0, to: count, by: 2).compactMap { UInt8(String(hex[$0..<$0.advanced(by: 2)]), radix: 16) }
    }
}

public class QRGeneratorService {
    
    var backColor: UIColor = .clear
    var frontColor: UIColor = .black
    
    public required init(backColor: UIColor = .clear, frontColor: UIColor = .black) {
        self.backColor = backColor
        self.frontColor = frontColor
    }
    
    /// Generate QR Code UIImage object with desired CGSize
    ///
    /// - Parameters:
    ///   - string: input String object
    ///   - size: input CGSize object
    /// - Returns: optional UIImage object
    public func generateQRCode(fromString messageData: String, withImageSize size: CGSize) -> UIImage? {
        let data = messageData.data(using: String.Encoding.isoLatin1, allowLossyConversion: false)
        
        if let filter = CIFilter(name: "CIQRCodeGenerator") {
            filter.setDefaults()
            filter.setValue(data, forKey: "inputMessage")
            filter.setValue("Q", forKey: "inputCorrectionLevel")
            
            guard let filterOutputImage = outputImageFromFilter(filter: filter) else { return nil }
            guard let outputImage = imageWithImageFilter(inputImage: filterOutputImage) else { return nil }

            return createNonInterpolatedImageFromCIImage(image: outputImage, size: size)
        }
        
        return nil
    }
    
    /// Create raw CIImage object from CIFilter
    ///
    /// - Parameter filter: input CIFilter
    /// - Returns: optional CIImage object
    private func outputImageFromFilter(filter: CIFilter) -> CIImage? {
        return filter.value(forKey: "outputImage") as? CIImage ?? nil
    }
    
    /// Apply color filters to CIImage object
    ///
    /// - Parameter inputImage: input CIImage object
    /// - Returns: optional CIImage object
    private func imageWithImageFilter(inputImage: CIImage) -> CIImage? {
        if let colorFilter = CIFilter(name: "CIFalseColor") {
            colorFilter.setDefaults()
            colorFilter.setValue(inputImage, forKey: "inputImage")
            colorFilter.setValue(CIColor(cgColor: backColor.cgColor), forKey: "inputColor1")
            colorFilter.setValue(CIColor(cgColor: frontColor.cgColor), forKey: "inputColor0")
            return outputImageFromFilter(filter: colorFilter)
        }
        return nil
    }
    
    /// Resize CIImage object with new size
    ///
    /// - Parameters:
    ///   - image: input CIImage object
    ///   - size: imagesize
    /// - Returns: optional UIImage object
    private func createNonInterpolatedImageFromCIImage(image: CIImage, size: CGSize) -> UIImage? {
        
        #if (arch(i386) || arch(x86_64))
        let contextOptions = [CIContextOption.useSoftwareRenderer: false]
        #else
        let contextOptions = [CIContextOption.useSoftwareRenderer: true]
        #endif
        
        guard let cgImage = CIContext(options: contextOptions).createCGImage(image, from: image.extent) else { return nil }
        UIGraphicsBeginImageContextWithOptions(size, false, 0.0)
        guard let context = UIGraphicsGetCurrentContext() else { return nil }
        
        context.interpolationQuality = CGInterpolationQuality.none
        context.setShouldAntialias(false)
        
        context.draw(cgImage, in: context.boundingBoxOfClipPath)
        guard let newImage = UIGraphicsGetImageFromCurrentImageContext() else {
            UIGraphicsEndImageContext()
            return nil
        }
        UIGraphicsEndImageContext()
        return newImage
    }
}
