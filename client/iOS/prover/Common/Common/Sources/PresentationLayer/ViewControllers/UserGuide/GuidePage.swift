import UIKit

public struct GuidePage: Equatable {
    public let image: UIImage
    public let title: String
    
    public init(image: UIImage, title: String) {
        self.image = image
        self.title = title
    }
}
