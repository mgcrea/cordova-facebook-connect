//
//  FacebookConnect.m
//
// Created by Olivier Louvignes on 15/06/2012.
//
// Copyright 2012 Olivier Louvignes. All rights reserved.
// MIT Licensed

#import "FacebookConnect.h"
#ifdef CORDOVA_FRAMEWORK
	#import <Cordova/JSONKit.h>
#else
	#import "JSONKit.h"
#endif

@implementation FacebookConnect

@synthesize callbackIds = _callbackIds;
@synthesize appId = _appId;
@synthesize facebook = _facebook;
@synthesize facebookRequests = _facebookRequests;
@synthesize dateFormatter = _dateFormatter;

#pragma mark -
#pragma mark Custom getters & setters

- (NSMutableDictionary *)callbackIds {
	if(_callbackIds == nil) {
		_callbackIds = [[NSMutableDictionary alloc] init];
	}
	return _callbackIds;
}
- (Facebook *)facebook {
	if([self.appId length] == 0) {
		ALog(@"ERROR: You must provide a non-empty appId.");
	}
	if(_facebook == nil) {
		_facebook = [[Facebook alloc] initWithAppId:self.appId andDelegate:self];
	}
	return _facebook;
}
- (NSMutableDictionary *)facebookRequests {
	if(_facebookRequests == nil) {
		_facebookRequests = [[NSMutableDictionary alloc] init];
	}
	return _facebookRequests;
}
- (NSDateFormatter *)dateFormatter {
	if(_dateFormatter == nil) {
		_dateFormatter = [[NSDateFormatter alloc] init];
		[_dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
		[_dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZ"];
	}
	return _dateFormatter;
}

#pragma mark -
#pragma mark Cordova plugin interface

- (void)initWithAppId:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options {
	DLog(@"initWithAppId:%@\n withDict:%@", arguments, options);

	// The first argument in the arguments parameter is the callbackId.
	[self.callbackIds setValue:[arguments pop] forKey:@"initWithAppId"];
	self.appId = [options objectForKey:@"appId"] ?: @"";

	NSMutableDictionary *result = [[[NSMutableDictionary alloc] init] autorelease];
	[result setObject:self.appId forKey:@"appId"];

	// Check for any stored session update Facebook session information
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
	if ([defaults objectForKey:@"FBAccessTokenKey"] && [defaults objectForKey:@"FBExpirationDateKey"]) {
        self.facebook.accessToken = [defaults objectForKey:@"FBAccessTokenKey"];
        self.facebook.expirationDate = [defaults objectForKey:@"FBExpirationDateKey"];

		// Build returned result
		[result setObject:self.facebook.accessToken forKey:@"accessToken"];
		[result setObject:[self.dateFormatter stringFromDate:self.facebook.expirationDate] forKey:@"expirationDate"];
	}

	CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
	[self writeJavascript:[pluginResult toSuccessCallbackString:[self.callbackIds valueForKey:@"initWithAppId"]]];

}

- (void)login:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options {
	DLog(@"login:%@\n withDict:%@", arguments, options);

	// The first argument in the arguments parameter is the callbackId.
	[self.callbackIds setValue:[arguments pop] forKey:@"login"];
	NSMutableArray *permissions = [options objectForKey:@"permissions"] ?: [[NSMutableArray alloc] init];

	if([options objectForKey:@"appId"]) {
		self.appId = [options objectForKey:@"appId"];
		// Check for any stored session update Facebook session information
		NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
		if ([defaults objectForKey:@"FBAccessTokenKey"] && [defaults objectForKey:@"FBExpirationDateKey"]) {
			self.facebook.accessToken = [defaults objectForKey:@"FBAccessTokenKey"];
			self.facebook.expirationDate = [defaults objectForKey:@"FBExpirationDateKey"];
		}
	}

	if (![self.facebook isSessionValid]) {
		[self.facebook authorize:permissions];
	} else {
		[self.facebookRequests setValue:[self.facebook requestWithGraphPath:@"me" andDelegate:self]
								 forKey:[self.callbackIds valueForKey:@"login"]];
	}

  [permissions release];
}

- (void)requestWithGraphPath:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options {
	DLog(@"requestWithGraphPath:%@\n withDict:%@", arguments, options);

	// The first argument in the arguments parameter is the callbackId.
	[self.callbackIds setValue:[arguments pop] forKey:@"requestWithGraphPath"];
	NSString *path = [options objectForKey:@"path"] ?: @"me";

	[self.facebookRequests setValue:[self.facebook requestWithGraphPath:path andDelegate:self]
							 forKey:[self.callbackIds valueForKey:@"requestWithGraphPath"]];

}

- (void)logout:(NSMutableArray *)arguments withDict:(NSMutableDictionary *)options {
	DLog(@"logout:%@\n withDict:%@", arguments, options);

	// The first argument in the arguments parameter is the callbackId.
	[self.callbackIds setValue:[arguments pop] forKey:@"logout"];
	[self.facebook logout];

}

#pragma mark -
#pragma mark < FBSessionDelegate >

- (void) handleOpenURL:(NSNotification *)notification {
	NSURL* url = [notification object];
	if (![url isKindOfClass:[NSURL class]]) {
        return;
	}
    [self.facebook handleOpenURL:url];
}

- (void)fbDidLogin {
	DLog(@"fbDidLogin");

	// Update session information in NSUserDefaults
	NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setObject:[self.facebook accessToken] forKey:@"FBAccessTokenKey"];
    [defaults setObject:[self.facebook expirationDate] forKey:@"FBExpirationDateKey"];
    [defaults synchronize];

	// Perform initial graph request
	[self.facebookRequests setValue:[self.facebook requestWithGraphPath:@"me" andDelegate:self]
							 forKey:[self.callbackIds valueForKey:@"login"]];

}

- (void)fbDidNotLogin:(BOOL)cancelled {
	DLog(@"fbDidNotLogin:%d", (int)cancelled);

	NSMutableDictionary *result = [[[NSMutableDictionary alloc] init] autorelease];
	[result setObject:(cancelled ? @"1" : @"0") forKey:@"cancelled"];

	CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:result];
	[self writeJavascript:[pluginResult toErrorCallbackString:[self.callbackIds valueForKey:@"login"]]];
}

- (void)fbDidExtendToken:(NSString *)accessToken expiresAt:(NSDate *)expiresAt {
	DLog(@"fbDidExtendToken:%@\n expiresAt:%@", accessToken, expiresAt);

	// Update session information in NSUserDefaults
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setObject:accessToken forKey:@"FBAccessTokenKey"];
    [defaults setObject:expiresAt forKey:@"FBExpirationDateKey"];
    [defaults synchronize];

}

- (void)fbDidLogout {

    // Cleared stored session information
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults removeObjectForKey:@"FBAccessTokenKey"];
    [defaults removeObjectForKey:@"FBExpirationDateKey"];
    [defaults synchronize];

	CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
	[self writeJavascript:[pluginResult toSuccessCallbackString:[self.callbackIds valueForKey:@"logout"]]];

}

- (void)fbSessionInvalidated {}

#pragma mark -
#pragma mark < FBRequestDelegate >

/**
 * Called when the Facebook API request has returned a response. This callback
 * gives you access to the raw response. It's called before
 * (void)request:(FBRequest *)request didLoad:(id)result,
 * which is passed the parsed response object.
 */
- (void)request:(FBRequest *)request didReceiveResponse:(NSURLResponse *)response {
	//DLog(@"request:%@\n didReceiveResponse:%@", request, response);
}

/**
 * Called when a request returns and its response has been parsed into
 * an object. The resulting object may be a dictionary, an array, a string,
 * or a number, depending on the format of the API response. If you need access
 * to the raw response, use:
 *
 * (void)request:(FBRequest *)request
 *      didReceiveResponse:(NSURLResponse *)response
 */
- (void)request:(FBRequest *)request didLoad:(id)result {
	DLog(@"request:%@\n didLoad:%@", request, result);

	// Loop through facebookRequests to find matching one
	NSString *matchingCallbackId = nil;
	for (id key in self.facebookRequests) {
		id value = [self.facebookRequests objectForKey:key];
		if(request == value) matchingCallbackId = key;
	}

	if ([result isKindOfClass:[NSDictionary class]]) {

		NSMutableDictionary *mutableResult = [result mutableCopy];
		[mutableResult setObject:self.facebook.accessToken forKey:@"accessToken"];
		[mutableResult setObject:[self.dateFormatter stringFromDate:self.facebook.expirationDate] forKey:@"expirationDate"];

		CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:mutableResult];
		[self writeJavascript:[pluginResult toSuccessCallbackString:matchingCallbackId]];

		[mutableResult release];
	} else if ([result isKindOfClass:[NSData class]]) {
        DLog(@"Unsupported result... %@", result);
        //[profilePicture release];
        //profilePicture = [[UIImage alloc] initWithData: result];
    } else {
		DLog(@"Unsupported result... %@", result);
	}

};

/**
 * Called when an error prevents the Facebook API request from completing
 * successfully.
 */
- (void)request:(FBRequest *)request didFailWithError:(NSError *)error {
	DLog(@"request:%@\n didFailWithError:%@", request, error);

	// Loop through facebookRequests to find matching one
	NSString *matchingCallbackId = nil;
	for (id key in self.facebookRequests) {
		id value = [self.facebookRequests objectForKey:key];
		if(request == value) matchingCallbackId = key;
	}

	CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error localizedDescription]];
	[self writeJavascript:[pluginResult toErrorCallbackString:matchingCallbackId]];
};

#pragma mark -
#pragma mark < FBDialogDelegate >

/**
 * Called when a UIServer Dialog successfully return.
 */
- (void)dialogDidComplete:(FBDialog *)dialog {
	DLog(@"dialogDidComplete:%@", dialog);
}

#pragma mark -
#pragma mark Memory management

- (void) dealloc {
	[_callbackIds release];
	[_appId release];
	[_facebook release];
	[_facebookRequests release];
	[_dateFormatter release];
	[super dealloc];
}

@end
