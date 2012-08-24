var app = {
    initialize: function() {
        this.bind();
    },
    bind: function() {
        document.addEventListener('deviceready', this.deviceready, false);
    },
    deviceready: function() {
        // note that this is an event handler so the scope is that of the event
        // so we need to call app.report(), and not this.report()
        app.report('deviceready');
    },
    report: function(id) {
        console.log("report:" + id);
        // hide the .pending <p> and show the .complete <p>
        document.querySelector('#' + id + ' .pending').className += ' hide';
        var completeElem = document.querySelector('#' + id + ' .complete');
        completeElem.className = completeElem.className.split('hide').join('');
    }
};

var plugin = {
    login: function() {
        var appId = "317511395000921";
        window.plugins.facebookConnect.login({permissions: ["email", "user_about_me"], appId: appId}, function(result) {
            console.log("FacebookConnect.login:" + JSON.stringify(result));
        });
    },
    requestWithGraphPath: function() {
        window.plugins.facebookConnect.requestWithGraphPath("/me/friends", function(result) {
            console.log("FacebookConnect.requestWithGraphPath:" + JSON.stringify(result));
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
            console.log("FacebookConnect.dialog:" + JSON.stringify(response));
        });
    },
    logout : function() {
        window.plugins.facebookConnect.logout(function(result) {
            console.log("FacebookConnect.logout:" + JSON.stringify(result));
        });
    }
};
