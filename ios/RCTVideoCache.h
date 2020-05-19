#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <SPTPersistentCache/SPTPersistentCache.h>
#import <SPTPersistentCache/SPTPersistentCacheOptions.h>
#import <CommonCrypto/CommonDigest.h>

typedef NS_ENUM(NSUInteger, RCTVideoCacheStatus) {
  RCTVideoCacheStatusMissingFileExtension,
  RCTVideoCacheStatusUnsupportedFileExtension,
  RCTVideoCacheStatusNotAvailable,
  RCTVideoCacheStatusAvailable
};

@class SPTPersistentCache;
@class SPTPersistentCacheOptions;

@interface RCTVideoCacheE : NSObject
{
  SPTPersistentCache *videoCache;
  NSString * _Nullable cachePath;
  NSString * temporaryCachePath;
  NSString * _Nullable cacheIdentifier;
}

@property(nonatomic, strong) SPTPersistentCache * _Nullable videoCache;
@property(nonatomic, strong) NSString * temporaryCachePath;

+ (RCTVideoCacheE *)sharedInstance;
- (void)storeItem:(NSData *)data forUri:(NSString *)uri withCallback:(void(^)(BOOL))handler;
- (void)getItemForUri:(NSString *)url withCallback:(void(^)(RCTVideoCacheStatus, AVAsset * _Nullable)) handler;
- (NSURL *)createUniqueTemporaryFileUrl:(NSString * _Nonnull)url withExtension:(NSString * _Nonnull) extension;
- (AVURLAsset *)getItemFromTemporaryStorage:(NSString *)key;
- (BOOL)saveDataToTemporaryStorage:(NSData *)data key:(NSString *)key;
- (void) createTemporaryPath;
- (void) setVideoCache:(SPTPersistentCache *)videoCache;

@end
