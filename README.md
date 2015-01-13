# Cordova Facebook Connect Plugin #
originally by [Olivier Louvignes](http://olouv.com)
forked and updated to work with modern Cordova and Facebook 2.2+ API by [Kris Erickson][https://github.com/kriserickson]

###Note: The IOS branch has been untested in over a year but will be updated soon.###

## DESCRIPTION ##

* This plugin provides a simple way to use Facebook Graph API in Cordova.

* This plugin is built for Cordova >= v3.3.0. Both iOS & Android are supported with the same javascript interface.

* For iOS, this plugin relies on the [Facebook iOS SDK](https://github.com/facebook/facebook-ios-sdk) that is bundled in the `FacebookSDK` folder (licensed under the Apache License, Version 2.0).

* For Android, this plugin relies uses the included MinimalFacebookSDK which as a JAR is 60K rather than 1.5Megs which the full Facebook SDK compiles to as a JAR.

* Regarding the existing implementation : [phonegap-plugin-facebook-connect](https://github.com/davejohnson/phonegap-plugin-facebook-connect) built by Dave Johnson, this version does not require the Facebook JS sdk (redundant to native sdk). It is also quite easier to use (unified login & initial /me request) and it does support multiple graph requests (strong callback handling).

## SAMPLE PROJECT GENERATION ##

You can generate a sample XCode project by running `samples/ios/create.sh` from the root of the repository.

## PLUGIN SETUP FOR IOS ##

Using this plugin requires [Cordova iOS](https://github.com/apache/incubator-cordova-ios).

1. Make sure your Xcode project has been [updated for Cordova](https://github.com/apache/incubator-cordova-ios/blob/master/guides/Cordova%20Upgrade%20Guide.md)
2. Rename the `ios` folder to `FacebookConnect`, drag and drop it from Finder to your Plugins folder in XCode, using "Create groups for any added folders"
3. Add the .js files to your `www` folder on disk, and add reference(s) to the .js files using `<script>` tags in your html file(s)


    `<script type="text/javascript" src="/js/plugins/FacebookConnect.js"></script>`


4. Add new entry with key `FacebookConnect` and value `FacebookConnect` to `Plugins` in `Cordova.plist/Cordova.plist`

5. In the Build Settings tab, search for Other Linker Flags. Add the value `-lsqlite3.0`.

6. In the Build Phases tab, link binary with libraries section. Add the Social.framework, Accounts.framework and AdSupport.framework frameworks. Make them optional to support pre-iOS6 devices.

7. Modify your application .plist according to the [Facebook iOS : Getting started guide](https://developers.facebook.com/docs/getting-started/getting-started-with-the-ios-sdk/#project), check the `Modify the app property list file` section.

>
   create an array key called URL types with a single array sub-item called URL Schemes. Give this a single item with your app ID prefixed with fb: [ScreenShot](https://developers.facebook.com/attachment/iosappid2.png). This is used to ensure the application will receive the callback URL of the web-based OAuth flow.

## PLUGIN SETUP FOR ANDROID ##

Using this plugin requires [Cordova Android](http://cordova.apache.org/).

1. Add the cordova plugin (see [Plugin Guide](http://cordova.apache.org/docs/en/edge/guide_cli_index.md.html))

     `cordova plugin add https://github.com/mgcrea/cordova-facebook-connect`

2. Add your Key Hashes (see [Android Getting Started](https://developers.facebook.com/docs/android/getting-started/) for more details).

## JAVASCRIPT INTERFACE (IOS/ANDROID) ##

    // After device ready, create a local alias
    var facebookConnect = window.plugins.facebookConnect;

    facebookConnect.login({permissions: ["email", "user_about_me"], appId: "YOUR_APP_ID"}, function(result) {
        console.log("FacebookConnect.login:" + JSON.stringify(result));

        // Check for cancellation/error
        if(result.cancelled || result.error) {
            console.log("FacebookConnect.login:failedWithError:" + result.message);
            return;
        }

        // Basic graph request example
        facebookConnect.requestWithGraphPath("/me/friends", {limit: 100}, function(result) {
            console.log("FacebookConnect.requestWithGraphPath:" + JSON.stringify(result));
        });

        // Feed dialog example
        var dialogOptions = {
            link: 'https://developers.facebook.com/docs/reference/dialogs/',
            picture: 'http://fbrell.com/f8.jpg',
            name: 'Facebook Dialogs',
            caption: 'Reference Documentation',
            description: 'Using Dialogs to interact with users.'
        };
        facebookConnect.dialog('feed', dialogOptions, function(response) {
            console.log("FacebookConnect.dialog:" + JSON.stringify(response));
        });

    });

## LICENSE ##

Copyright 2012 Olivier Louvignes. All rights reserved.

The MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## CREDITS ##

Contributors :

* [Jon Buffington](http://blog.jon.buffington.name/) added dialog support for iOS.

Inspired by :

* [phonegap-plugin-facebook-connect](https://github.com/davejohnson/phonegap-plugin-facebook-connect) built by Dave Johnson.

* [Facebook iOS Tutorial](https://developers.facebook.com/docs/mobile/ios/build/)

* [Facebook iOS SDK Reference](https://developers.facebook.com/docs/reference/iossdk/)
