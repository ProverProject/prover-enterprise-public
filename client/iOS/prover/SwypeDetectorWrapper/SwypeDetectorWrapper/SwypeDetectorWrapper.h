#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

#ifdef __cplusplus
#define TEMP_NO NO
#undef NO
#include "swype_detect.h"
#define NO TEMP_NO
#undef TEMP_NO
#endif

@interface SwypeDetectorWrapper: NSObject

- (instancetype) init;

-(void)setSwype:(NSString*)swype;

-(int)getSwypeHelperVersion;

-(void)processFrame:(CVImageBufferRef)imageBuffer
          timestamp:(uint)timestamp
              state:(int*)state
              index:(int*)index
                  x:(int*)x
                  y:(int*)y
            message:(int*)message
              debug:(int*)debug;

-(double) detectorXScale;
-(double) detectorYScale;

@end
