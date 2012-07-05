//
//  FacebookConnect.h
//
// Created by Olivier Louvignes on 2012-06-25.
//
// Copyright 2012 Olivier Louvignes. All rights reserved.
// MIT Licensed

#import <Foundation/Foundation.h>
#ifdef CORDOVA_FRAMEWORK
	#import <Cordova/CDVPlugin.h>
#else
	#import "CDVPlugin.h"
#endif

#import "FBConnect.h"

@interface FacebookConnect : CDVPlugin < FBSessionDelegate, FBRequestDelegate, FBDialogDelegate > {

	NSMutableDictionary* _callbackIds;
	NSString* _appId;
	Facebook* _facebook;
	NSMutableDictionary* _facebookRequests;
	NSDateFormatter* _dateFormatter;

}

#pragma mark -
#pragma mark Properties

@property (nonatomic, retain) NSMutableDictionary *callbackIds;
@property (nonatomic, copy) NSString *appId;
@property (nonatomic, retain) Facebook *facebook;
@property (nonatomic, retain) NSMutableDictionary *facebookRequests;
@property (nonatomic, retain) NSDateFormatter *dateFormatter;

#pragma mark -
#pragma mark Instance methods

- (void)initWithAppId:(NSMutableArray *)arguments withDict:(NSMutableDictionary*)options;
- (void)login:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options;
- (void)requestWithGraphPath:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options;
- (void)logout:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options;

@end

#pragma mark -
#pragma mark Logging tools

#ifdef DEBUG
#   define DLog(fmt, ...) NSLog((@"%s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);
#else
#   define DLog(...)
#endif
#define ALog(fmt, ...) NSLog((@"%s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);
