//
//  RCTVideoPlayerViewController.h
//  RCTVideo
//
//  Created by Stanisław Chmiela on 31.03.2016.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import <AVKit/AVKit.h>
#import "RCTVideoE.h"
#import "RCTVideoEPlayerViewControllerDelegate.h"

@interface RCTVideoPlayerViewControllerE : AVPlayerViewController
@property (nonatomic, weak) id<RCTVideoPlayerViewControllerDelegate> rctDelegate;

// Optional paramters
@property (nonatomic, weak) NSString* preferredOrientation;
@property (nonatomic) BOOL autorotate;

@end
