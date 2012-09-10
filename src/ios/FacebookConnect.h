//
//  FacebookConnect.h
//
// Created by Olivier Louvignes on 2012-06-25.
//
// Copyright 2012 Olivier Louvignes. All rights reserved.
// MIT Licensed

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import <FacebookSDK/FacebookSDK.h>
#import <FacebookSDK/Facebook.h>

@interface FacebookConnect : CDVPlugin <FBSessionDelegate, FBRequestDelegate, FBDialogDelegate> {
}

#pragma mark - Properties

@property (nonatomic, retain) NSMutableDictionary *callbackIds;
@property (nonatomic, copy) NSString *appId;
@property (nonatomic, retain) Facebook *facebook;
@property (nonatomic, retain) NSMutableDictionary *facebookRequests;
@property (nonatomic, retain) NSDateFormatter *dateFormatter;

#pragma mark - Instance methods

- (void)initWithAppId:(NSMutableArray *)arguments withDict:(NSMutableDictionary*)options;
- (void)login:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options;
- (void)requestWithGraphPath:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options;
- (void)logout:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options;
- (void)dialog:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options;

@end

#pragma mark - Logging tools

#ifdef DEBUG
#   define DLog(fmt, ...) NSLog((@"%s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);
#else
#   define DLog(...)
#endif
#define ALog(fmt, ...) NSLog((@"%s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);
