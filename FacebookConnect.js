//
//  FacebookConnect.js
//
// Created by Olivier Louvignes on 2012-06-25.
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

	/**
	 * Make an asynchrous Facebook Graph API request.
	 *
	 * @param {String} path Is the path to the Graph API endpoint.
	 * @param {Object} [options] Are optional key-value string pairs representing the API call parameters.
	 * @param {String} [httpMethod] Is an optional HTTP method that defaults to GET.
	 * @param {Function} [callback] Is an optional callback method that receives the results of the API call.
	 */
	FacebookConnect.prototype.requestWithGraphPath = function(path, options, httpMethod, callback) {
		var method;

		if(!path) path = "me";
		if(typeof options === 'function') {
			callback = options;
			options = {};
			httpMethod = undefined;
		}
		if (typeof httpMethod === 'function') {
			callback = httpMethod;
			httpMethod = undefined;
		}
		httpMethod = httpMethod || 'GET';

		var _callback = function(result) {
			//console.log('FacebookConnect.requestWithGraphPath: %o', arguments);
			if(typeof callback == 'function') callback.apply(null, arguments);
		};

		return cordova.exec(_callback, _callback, service, 'requestWithGraphPath', [{path: path, options: options, httpMethod: httpMethod}]);

	};

	FacebookConnect.prototype.logout = function(callback) {

		var _callback = function(logout) {
			//console.log('FacebookConnect.logout: %o', arguments);
			if(typeof callback == 'function') callback.apply(null, arguments);
		};

		return cordova.exec(_callback, _callback, service, 'logout', []);

	};

	FacebookConnect.prototype.dialog = function(method, options, callback) {

		var _callback = function(result) {
			//console.log('FacebookConnect.dialog: %o', arguments);
			if(typeof callback == 'function') callback.apply(null, arguments);
		};

		return cordova.exec(_callback, _callback, service, 'dialog', [{method: method, params: options}]);

	};

	cordova.addConstructor(function() {
		if(!window.plugins) window.plugins = {};
		window.plugins.facebookConnect = new FacebookConnect();
	});

})(window.cordova || window.Cordova);
