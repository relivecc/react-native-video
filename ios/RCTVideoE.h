#import <AVFoundation/AVFoundation.h>
#import "AVKit/AVKit.h"
#import "UIView+FindUIViewController.h"
#import "RCTVideoEPlayerViewController.h"
#import "RCTVideoEPlayerViewControllerDelegate.h"
#import <React/RCTComponent.h>
#import <React/RCTBridgeModule.h>

#import "RCTVideoCacheE.h"
#import "DVURLAsset.h"
#import "DVAssetLoaderDelegate.h"

@class RCTEventDispatcher;
#if __has_include(<react-native-video-exp/RCTVideoCacheE.h>)
@interface RCTVideoE : UIView <RCTVideoPlayerViewControllerDelegate, DVAssetLoaderDelegatesDelegate>
#elif TARGET_OS_TV
@interface RCTVideoE : UIView <RCTVideoPlayerViewControllerDelegate>
#else
@interface RCTVideoE : UIView <RCTVideoPlayerViewControllerDelegate, AVPictureInPictureControllerDelegate>
#endif

@property (nonatomic, copy) RCTDirectEventBlock onVideoLoadStart;
@property (nonatomic, copy) RCTDirectEventBlock onVideoLoad;
@property (nonatomic, copy) RCTDirectEventBlock onVideoBuffer;
@property (nonatomic, copy) RCTDirectEventBlock onVideoError;
@property (nonatomic, copy) RCTDirectEventBlock onVideoProgress;
@property (nonatomic, copy) RCTDirectEventBlock onBandwidthUpdate;
@property (nonatomic, copy) RCTDirectEventBlock onVideoSeek;
@property (nonatomic, copy) RCTDirectEventBlock onVideoEnd;
@property (nonatomic, copy) RCTDirectEventBlock onTimedMetadata;
@property (nonatomic, copy) RCTDirectEventBlock onVideoAudioBecomingNoisy;
@property (nonatomic, copy) RCTDirectEventBlock onVideoFullscreenPlayerWillPresent;
@property (nonatomic, copy) RCTDirectEventBlock onVideoFullscreenPlayerDidPresent;
@property (nonatomic, copy) RCTDirectEventBlock onVideoFullscreenPlayerWillDismiss;
@property (nonatomic, copy) RCTDirectEventBlock onVideoFullscreenPlayerDidDismiss;
@property (nonatomic, copy) RCTDirectEventBlock onReadyForDisplay;
@property (nonatomic, copy) RCTDirectEventBlock onPlaybackStalled;
@property (nonatomic, copy) RCTDirectEventBlock onPlaybackResume;
@property (nonatomic, copy) RCTDirectEventBlock onPlaybackRateChange;
@property (nonatomic, copy) RCTDirectEventBlock onVideoExternalPlaybackChange;
@property (nonatomic, copy) RCTDirectEventBlock onPictureInPictureStatusChanged;
@property (nonatomic, copy) RCTDirectEventBlock onRestoreUserInterfaceForPictureInPictureStop;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

- (AVPlayerViewController*)createPlayerViewController:(AVPlayer*)player withPlayerItem:(AVPlayerItem*)playerItem;

- (void)save:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
+ (void)export:(NSString *)url resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;

@end
