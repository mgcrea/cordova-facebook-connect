
var plugin = {
    login: function() {
        var appId = "317511395000921";
        window.plugins.facebookConnect.login({permissions: ["email", "user_about_me"], appId: appId}, function(result) {
            console.log("facebookConnect.login:" + JSON.stringify(result));
        });
    },
    requestWithGraphPath: function() {
        window.plugins.facebookConnect.requestWithGraphPath("/me/friends", function(result) {
            console.log("facebookConnect.requestWithGraphPath:" + JSON.stringify(result));
        });
    },
    dialog : function() {
        var dialogOptions = {
            link: 'https://developers.facebook.com/docs/reference/dialogs/',
            picture: 'http://fbrell.com/f8.jpg',
            name: 'Facebook Dialogs',
            caption: 'Reference Documentation',
            description: 'Using Dialogs to interact with users.'
        };

        window.plugins.facebookConnect.dialog('feed', dialogOptions, function(response) {
            console.log("facebookConnect.dialog:" + JSON.stringify(response));
        });
    },
    logout : function() {
        window.plugins.facebookConnect.logout(function(result) {
            console.log("facebookConnect.logout:" + JSON.stringify(result));
        });
    }
};
