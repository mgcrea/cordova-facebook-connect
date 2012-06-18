//
//  FacebookConnect.js
//
// Created by Olivier Louvignes on 15/06/2012.
//
// Copyright 2012 Olivier Louvignes. All rights reserved.
// MIT Licensed

(function(cordova) {

	function FacebookConnect() {}
	var service = 'FacebookConnect';

	FacebookConnect.prototype.initWithAppId = function(appId, callback) {
		if(!appId) return false;

		var _callback = function(result) {
			//console.log('FacebookConnect.initWithAppId: %o', arguments);
			if(typeof callback == 'function') callback.apply(null, arguments);
		};

		return cordova.exec(_callback, _callback, service, 'initWithAppId', [{appId: appId}]);

	};

	FacebookConnect.prototype.login = function(options, callback) {
		if(!options) options = {};

		var config = {
			permissions: options.permissions || ['email'],
			appId: options.appId || ''
		};

		var _callback = function(result) {
			//console.log('FacebookConnect.login: %o', arguments);
			if(typeof callback == 'function') callback.apply(null, arguments);
		};

		return cordova.exec(_callback, _callback, service, 'login', [config]);

	};

	FacebookConnect.prototype.requestWithGraphPath = function(path, callback) {
		if(!path) path = "me";

		var _callback = function(result) {
			//console.log('FacebookConnect.requestWithGraphPath: %o', arguments);
			if(typeof callback == 'function') callback.apply(null, arguments);
		};

		return cordova.exec(_callback, _callback, service, 'requestWithGraphPath', [{path: path}]);

	};

	FacebookConnect.prototype.logout = function(callback) {

		var _callback = function(logout) {
			//console.log('FacebookConnect.logout: %o', arguments);
			if(typeof callback == 'function') callback.apply(null, arguments);
		};

		return cordova.exec(_callback, _callback, service, 'logout', []);

	};

	cordova.addConstructor(function() {
		if(!window.plugins) window.plugins = {};
		window.plugins.facebookConnect = new FacebookConnect();
	});

})(window.cordova || window.Cordova);
