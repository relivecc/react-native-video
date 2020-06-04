#import "RCTVideoCacheE.h"

@implementation RCTVideoCacheE

@synthesize videoCache;
@synthesize temporaryCachePath;

+ (RCTVideoCacheE *)sharedInstance {
  static RCTVideoCacheE *sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [[self alloc] init];
  });
  return sharedInstance;
}

- (id)init {
  if (self = [super init]) {
    self.temporaryCachePath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"video-tmp"];
      
    // The caching lbirary used by rn-video unfortunately forces creation of a temporary file.
    // Since we don't know when we're done with it we don't know when to delete it.
    // First possibility where we know it: app launch -> no video played or exported.
    // So we clear the video-tmp folder here if it exists.
    [self clearTemporaryIfExists];
    [self createTemporaryPath];
  }
  return self;
}

- (void) clearTemporaryIfExists {
    BOOL isDir;
    BOOL exists = [[NSFileManager defaultManager] fileExistsAtPath:self.temporaryCachePath isDirectory:&isDir];
    
    if (!exists) {
        return;
    }
    
    NSError *error = nil;
    [[NSFileManager defaultManager] removeItemAtPath:self.temporaryCachePath error:&error];
    
    if (error) {
        NSLog(@"Error clearing tmp-video cache! %@", error);
    }
}

- (void) createTemporaryPath {
  NSError *error = nil;
  BOOL success = [[NSFileManager defaultManager] createDirectoryAtPath:self.temporaryCachePath
                                           withIntermediateDirectories:YES
                                                            attributes:nil
                                                                 error:&error];
#ifdef DEBUG
  if (!success || error) {
    NSLog(@"Error while! %@", error);
  }
#endif
}

- (void)storeItem:(NSData *)data forUri:(NSString *)uri withCallback:(void(^)(BOOL))handler;
{
  NSString *key = [self generateCacheKeyForUri:uri];
  if (key == nil) {
    handler(NO);
    return;
  }

    
  [self saveDataToTemporaryStorage:data key:key];
  [self.videoCache storeData:data forKey:key locked:NO withCallback:^(SPTPersistentCacheResponse * _Nonnull response) {
    if (response.error) {
#ifdef DEBUG
      NSLog(@"An error occured while saving the video into the cache: %@", [response.error localizedDescription]);
#endif
      handler(NO);
      return;
    }
    handler(YES);
  } onQueue:dispatch_get_main_queue()];
  return;
}

- (AVURLAsset *)getItemFromTemporaryStorage:(NSString *)key {
  NSString * temporaryFilePath = [self.temporaryCachePath stringByAppendingPathComponent:key];
  
  BOOL fileExists = [[NSFileManager defaultManager] fileExistsAtPath:temporaryFilePath];
  if (!fileExists) {
    return nil;
  }
  NSURL *assetUrl = [[NSURL alloc] initFileURLWithPath:temporaryFilePath];
  AVURLAsset *asset = [AVURLAsset URLAssetWithURL:assetUrl options:nil];

  return asset;
}

- (BOOL)saveDataToTemporaryStorage:(NSData *)data key:(NSString *)key {
  NSString *temporaryFilePath = [self.temporaryCachePath stringByAppendingPathComponent:key];
  [data writeToFile:temporaryFilePath atomically:YES];
  return YES;
}

- (NSString *)generateCacheKeyForUri:(NSString *)uri {
    // Hash is different if we add query params -> Causes redownload for sharing, so we strip query from the url.
    NSURLComponents *components = [[NSURLComponents alloc] initWithString:uri];
    [components setQuery:nil];
    
    NSString *uriWithoutQueryParams = [components string];
    NSString * pathExtension = [uriWithoutQueryParams pathExtension];
    NSArray * supportedExtensions = @[@"m4v", @"mp4", @"mov"];
    if ([pathExtension isEqualToString:@""]) {
        NSDictionary *userInfo = @{
            NSLocalizedDescriptionKey: NSLocalizedString(@"Missing file extension.", nil),
            NSLocalizedFailureReasonErrorKey: NSLocalizedString(@"Missing file extension.", nil),
            NSLocalizedRecoverySuggestionErrorKey: NSLocalizedString(@"Missing file extension.", nil)
        };
        NSError *error = [NSError errorWithDomain:@"RCTVideoCache"
                                             code:RCTVideoCacheStatusMissingFileExtension userInfo:userInfo];
        @throw error;
    } else if (![supportedExtensions containsObject:pathExtension]) {
        // Notably, we don't currently support m3u8 (HLS playlists)
        NSDictionary *userInfo = @{
            NSLocalizedDescriptionKey: NSLocalizedString(@"Unsupported file extension.", nil),
            NSLocalizedFailureReasonErrorKey: NSLocalizedString(@"Unsupported file extension.", nil),
            NSLocalizedRecoverySuggestionErrorKey: NSLocalizedString(@"Unsupported file extension.", nil)
        };
        NSError *error = [NSError errorWithDomain:@"RCTVideoCache"
                                             code:RCTVideoCacheStatusUnsupportedFileExtension userInfo:userInfo];
        @throw error;
    }
    return [[self generateHashForUrl:uriWithoutQueryParams] stringByAppendingPathExtension:pathExtension];
}

- (void)getItemForUri:(NSString *)uri withCallback:(void(^)(RCTVideoCacheStatus, AVAsset * _Nullable)) handler {
  @try {
    NSString *key = [self generateCacheKeyForUri:uri];
    
    AVURLAsset * temporaryAsset = [self getItemFromTemporaryStorage:key];
    if (temporaryAsset != nil) {
      handler(RCTVideoCacheStatusAvailable, temporaryAsset);
      return;
    }
    
    [self.videoCache loadDataForKey:key withCallback:^(SPTPersistentCacheResponse * _Nonnull response) {
      if (response.record == nil || response.record.data == nil) {
        handler(RCTVideoCacheStatusNotAvailable, nil);
        return;
      }
      [self saveDataToTemporaryStorage:response.record.data key:key];
      handler(RCTVideoCacheStatusAvailable, [self getItemFromTemporaryStorage:key]);
    } onQueue:dispatch_get_main_queue()];
  } @catch (NSError * err) {
    switch (err.code) {
      case RCTVideoCacheStatusMissingFileExtension:
        handler(RCTVideoCacheStatusMissingFileExtension, nil);
        return;
      case RCTVideoCacheStatusUnsupportedFileExtension:
        handler(RCTVideoCacheStatusUnsupportedFileExtension, nil);
        return;
      default:
        @throw err;
    }
  }
}

- (NSString *)generateHashForUrl:(NSString *)string {
    const char *cStr = [string UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5( cStr, (CC_LONG)strlen(cStr), result );
    
    return [NSString stringWithFormat:
            @"%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X",
            result[0], result[1], result[2], result[3],
            result[4], result[5], result[6], result[7],
            result[8], result[9], result[10], result[11],
            result[12], result[13], result[14], result[15]
            ];
}

@end
