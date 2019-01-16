#import "SwypeDetectorWrapper.h"
#import <CoreImage/CoreImage.h>
#import <UIKit/UIKit.h>

@interface SwypeDetectorWrapper()

@property (nonatomic, assign) SwypeDetect *detector;
@property (nonatomic, assign) double xScale;
@property (nonatomic, assign) double yScale;
@property (nonatomic, assign) BOOL isSwypeDetectInitialized;

@end

@implementation SwypeDetectorWrapper

- (instancetype) init {
    self = [super init];
    _detector = new SwypeDetect();

    return self;
}

-(void)dealloc {
    delete _detector;
}

-(void)setSwype:(NSString*)swype {
    _detector->setSwype(std::string([swype UTF8String]));
}

-(int)getSwypeHelperVersion {
    return _detector->GetSwypeHelperVersion();
}

-(double) detectorXScale {
    return _xScale;
}

-(double) detectorYScale {
    return _yScale;
}

-(void)processFrame:(CVImageBufferRef)imageBuffer
          timestamp:(uint)timestamp
              state:(int *)state
              index:(int *)index
                  x:(int *)x
                  y:(int *)y
            message:(int *)message
              debug:(int *)debug {
    
    CVPixelBufferLockBaseAddress(imageBuffer, 0);
    
    int width = (int)CVPixelBufferGetWidth(imageBuffer);
    int height = (int)CVPixelBufferGetHeight(imageBuffer);
    size_t bytePerRow = CVPixelBufferGetBytesPerRow(imageBuffer);
    unsigned char *pixels = (unsigned char *) CVPixelBufferGetBaseAddress(imageBuffer);
    
    cv::Mat sourceMat = cv::Mat(height, width,
                                CV_8UC4, pixels,
                                bytePerRow);
    
    // save source image
//      NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
//      NSString *sourceFilePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"source_image.png"];
//      cv::imwrite(cv::String([sourceFilePath UTF8String]), sourceMat);
    //------------------
    
    cv::Mat grayMat;
    cv::cvtColor(sourceMat, grayMat, cv::COLOR_BGR2GRAY);
    // save gray image
//    NSString *grayFilePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"gray_image.png"];
//    cv::imwrite(cv::String([grayFilePath UTF8String]), grayMat);
    // ---------------

    if (!_isSwypeDetectInitialized) {
        double scale = sqrt(40000.0 / (width * height));

        _xScale = scale;
        _yScale = scale;
    }
    
    cv::Mat resizedMat;
    cv::resize(grayMat, resizedMat, cv::Size(), _xScale, _yScale, CV_INTER_LINEAR);
    // save resize image
//    NSString *resizeFilePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"resize_image.png"];
//    cv::imwrite(cv::String([resizeFilePath UTF8String]), resizeMat);
    //------------------
    
    if (!_isSwypeDetectInitialized) {
        _isSwypeDetectInitialized = true;

        int resizedWidth = resizedMat.cols;
        int resizedHeight = resizedMat.rows;

        _detector->init(//double(resizedWidth) / double(resizedHeight),
                        double(width) / double(height),
                        resizedWidth,
                        resizedHeight,
                        false);
    }
    
    CVPixelBufferUnlockBaseAddress(imageBuffer, 0);

    _detector->processMat(resizedMat, timestamp,
                          *state, *index, *x, *y,
                          *message, *debug);
}

@end
