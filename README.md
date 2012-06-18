# Cordova Facebook Connect Plugin #
by [Olivier Louvignes](http://olouv.com)

## DESCRIPTION ##

* This plugin provides a simple way to use Facebook Graph API in Cordova. It does comply with the latest (future-2.x) cordova standards.

* This plugin relies on the [Facebook iOS SDK](https://github.com/facebook/facebook-ios-sdk) that is bundled in the `Libraries` folder (licensed under the Apache License, Version 2.0).

* Main differences from the existing implementation [phonegap-plugin-facebook-connect](https://github.com/davejohnson/phonegap-plugin-facebook-connect) built by Dave Johnson is that it does not require the Facebook JS sdk (redundant to the iOS sdk bundled here). It is also quite easier to use (unified login & initial /me request) and it does support multiple graph requests (strong callback handling). However, the implementation is currently not fully finished (Missing dialog implementation) and iOS-only.

## PLUGIN SETUP ##

Using this plugin requires [Cordova iOS](https://github.com/apache/incubator-cordova-ios).

1. Make sure your Xcode project has been [updated for Cordova](https://github.com/apache/incubator-cordova-ios/blob/master/guides/Cordova%20Upgrade%20Guide.md)
2. Drag and drop the `FacebookConnect` folder from Finder to your Plugins folder in XCode, using "Create groups for any added folders"
3. Add the .js files to your `www` folder on disk, and add reference(s) to the .js files using `<script>` tags in your html file(s)


    `<script type="text/javascript" src="/js/plugins/FacebookConnect.js"></script>`


4. Add new entry with key `FacebookConnect` and value `FacebookConnect` to `Plugins` in `Cordova.plist/Cordova.plist`
5. Modify your application .plist according to the [Facebook iOS : Getting started guide](https://developers.facebook.com/docs/mobile/ios/build/), check the `Modify the app property list file` section.

    The last thing that needs to be accomplished to enable SSO support is a change to the .plist file that handles configuration for the app. Xcode creates this file automatically when the project is created. A specific URL needs to be registered in this file that uniquely identifies the app with iOS. Create a new row named URL types with a single item, URL Schemes, containing a single value, fbYOUR_APP_ID (the literal characters fb followed by your app ID). The following shows exactly how the row should appear in the .plist file:

## JAVASCRIPT INTERFACE ##

    // After device ready, create a local alias
    var facebookConnect = window.plugins.facebookConnect;

    facebookConnect.login({permissions: ["email", "user_about_me"], appId: "YOUR_APP_ID"}, function(result) {
        console.log("FacebookConnect.login:" + JSON.stringify(result));
        facebookConnect.requestWithGraphPath("/me/friends", function(result) {
            console.log("FacebookConnect.requestWithGraphPath:" + JSON.stringify(result));
        });
    });

* Check [source](https://github.com/mgcrea/cordova-facebook-connect/tree/master/FacebookConnect.js) for additional configuration.

## BUGS AND CONTRIBUTIONS ##

Patches welcome! Send a pull request. Since this is not a part of Cordova Core (which requires a CLA), this should be easier.

Post issues on [Github](https://github.com/mgcrea/cordova-facebook-connect/issues)

The latest code (my fork) will always be [here](https://github.com/mgcrea/cordova-facebook-connect/tree/master)

## LICENSE ##

Copyright 2012 Olivier Louvignes. All rights reserved.

The MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## CREDITS ##

Inspired by :

* [phonegap-plugin-facebook-connect](https://github.com/davejohnson/phonegap-plugin-facebook-connect) built by Dave Johnson.

* [Facebook iOS Tutorial](https://developers.facebook.com/docs/mobile/ios/build/)

* [Facebook iOS SDK Reference](https://developers.facebook.com/docs/reference/iossdk/)
