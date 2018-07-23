#import "RCTVideoCache.h"
#import <React/RCTBridgeModule.h>
#import <AVFoundation/AVFoundation.h>
#import "AVKit/AVKit.h"

// RCTVideoCache.m
@implementation RCTVideoCache

@synthesize videoCache;

// To export a module named RCTVideoCache
RCT_EXPORT_MODULE();

+ (id)sharedCache {
    static RCTVideoCache *sharedCache = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedCache = [[self alloc] init];
    });
    return sharedCache;
}

- (id)init {
  if (self = [super init]) {
      videoCache = [[NSMutableDictionary alloc] init];
  }
  return self;
}

- (void)dealloc {
  // Should never be called, but just here for clarity really.
}


RCT_EXPORT_METHOD(preloadVideo:(NSString *)url)
{
    NSLog(@"preloadVideo %@", url);

    RCTVideoCache *sharedCache = [RCTVideoCache sharedCache];
    if ([sharedCache.videoCache objectForKey:url]) {
        return;
    }

    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:[NSURL URLWithString:url] options:nil];
    NSArray *keys = @[@"playable", @"tracks", @"duration"];

    NSLog(@"Caching...");

    [asset loadValuesAsynchronouslyForKeys:keys completionHandler:^()
    {
        // make sure everything downloaded properly
        for (NSString *thisKey in keys) {
            NSError *error = nil;
            AVKeyValueStatus keyStatus = [asset statusOfValueForKey:thisKey error:&error];
            if (keyStatus != AVKeyValueStatusLoaded) {
                switch(keyStatus) {
                    case AVKeyValueStatusUnknown:
                        NSLog(@"Cache status unknown");
                    case AVKeyValueStatusLoading:
                        NSLog(@"Cache status loading");
                    case AVKeyValueStatusFailed:
                        NSLog(@"Cache status failed");
                    case AVKeyValueStatusCancelled:
                        NSLog(@"Cache status cancelled");
                }
                return;
            }
        }

        dispatch_async(dispatch_get_main_queue(), ^ {
            NSLog(@"Cache succeeded");
            // TODO it doesn't seem the entire video is loaded yet at this point
            sharedCache.videoCache[url] = asset;
        });
    }];
}

@end
