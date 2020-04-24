//
//  RCTVideoExport.m
//  react-native-video
//
#import "RCTVideoExport.h"
#import <Foundation/Foundation.h>
#include <AVFoundation/AVFoundation.h>

@implementation RCTVideoExport

+ (RCTVideoCache *)sharedInstance {
    static RCTVideoCache *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}


- (void)export:(NSString *)uri resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    // Thread to prevent stuttering.
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^(void){
        RCTVideoCache *videoCache = [RCTVideoCache sharedInstance];
        
        [videoCache getItemForUri:uri withCallback:^(RCTVideoCacheStatus videoCacheStatus, AVAsset * _Nullable cachedAsset) {
            NSURL *url = [NSURL URLWithString:uri];
            
            // If the asset is not in cache it is incomplete so we need to download it.
            if (cachedAsset == nil) {
                // Create a download session.
                NSURLSession *session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]];
                NSURLSessionDownloadTask *task = [session downloadTaskWithURL:url completionHandler:^(NSURL * _Nullable location, NSURLResponse * _Nullable response, NSError * _Nullable error) {
                    NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *) response;
                    
                    if (error != nil || httpResponse == nil) {
                        return reject(@"ERROR_COULD_NOT_DOWNLOAD_VIDEO", @"Could not download video.", error);
                    }
                    
                    if (httpResponse.statusCode != 200) {
                        return reject(@"ERROR_COULD_NOT_DOWNLOAD_VIDEO", @"Received status != 200.", error);
                    }
                                 
                    // AVAsset only works if file has .mp4, downloaded file has .tmp so we move it to temporary directory.
                    NSString *tempFile = [[[NSUUID UUID] UUIDString] stringByAppendingString:@".mp4"];
                    NSString *temporaryPath = [videoCache temporaryCachePath];
                    NSURL *temporaryVideoPath = [NSURL fileURLWithPath:[temporaryPath stringByAppendingString:tempFile]];
                    
                    NSError *err = nil;
                    NSFileManager *fileManager = [NSFileManager defaultManager];
                    [fileManager moveItemAtURL:location toURL:temporaryVideoPath error:&err];
                    
                    if (err != nil) {
                        return reject(@"ERROR_COULD_NOT_DOWNLOAD_VIDEO", @"Temporary file error.", err);
                    }

                    // Now move the downloaded file to cache.
                    [videoCache storeItem:[NSData dataWithContentsOfURL:temporaryVideoPath] forUri:uri withCallback:^(BOOL success) {
                        NSLog(@"Downloaded video and stored to video cache!");
                        
                        NSError *err = nil;
                        // Remove downloaded file, we don't care about the error here.
                        [fileManager removeItemAtURL:temporaryVideoPath error:&err];
                    }];
                    
                    // Now use the cached file for exporting.
                    [videoCache getItemForUri:uri withCallback:^(RCTVideoCacheStatus videoCacheStatus, AVAsset * _Nullable cachedAsset) {
                        if (cachedAsset) {
                            return [self assetExport:[AVAsset assetWithURL:temporaryVideoPath] resolve:resolve reject:reject];
                        }
                        
                        return reject(@"ERROR_COULD_NOT_DOWNLOAD_VIDEO", @"Cache error", nil);
                    }];
                }];
                
                [task resume];
                return;
            };
            
            [self assetExport:cachedAsset resolve:resolve reject:reject];
        }];
    });
}

- (void)assetExport:(AVAsset *)asset resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
      AVAssetExportSession *exportSession = [AVAssetExportSession exportSessionWithAsset:asset presetName:AVAssetExportPresetPassthrough];
      NSString *cachePath = [[RCTVideoCache sharedInstance] temporaryCachePath];
      
      if (exportSession == nil) {
          return reject(@"ERROR_COULD_NOT_CREATE_EXPORT_SESSION", @"Could not create export session", nil);
      }
      
      NSString *fileName = [[[NSUUID UUID] UUIDString] stringByAppendingString:@".mp4"];
      NSString *outputPath = [cachePath stringByAppendingPathComponent:fileName];
      NSURL *outputURL = [NSURL fileURLWithPath:outputPath];
      
      exportSession.outputFileType = AVFileTypeMPEG4;
      exportSession.outputURL = outputURL;
      exportSession.shouldOptimizeForNetworkUse = true;
      
      [exportSession exportAsynchronouslyWithCompletionHandler:^{
          if ([exportSession status] == AVAssetExportSessionStatusFailed) {
              return reject(@"ERROR_COULD_NOT_EXPORT_VIDEO", @"Could not export video", exportSession.error);
          } else if ([exportSession status] == AVAssetExportSessionStatusCancelled) {
              return reject(@"ERROR_EXPORT_SESSION_CANCELLED", @"Export session was cancelled", exportSession.error);
          } else {
              return resolve(@{@"uri": outputURL.absoluteString});
          }
      }];
}

@end
