//
//  RCTVideoExport.h
//  react-native-video-exp
//
#import <Foundation/Foundation.h>
#import <React/RCTComponent.h>
#import <React/RCTBridgeModule.h>
#import "RCTVideoCache.h"
#import "DVURLAsset.h"

@interface RCTVideoExportE : NSObject <AVAssetDownloadDelegate>

+ (RCTVideoExportE *)sharedInstance;
- (void)export:(NSString *)uri resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;

@end


