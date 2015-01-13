//
//  FacebookConnect.js
//
// Created by Olivier Louvignes on 2012-06-25.
// Updated for Cordova 3.+ Kris Erickson 2014-01-02
//
// Copyright 2012 Olivier Louvignes. All rights reserved.
// MIT Licensed



var exec = require('cordova/exec');

var service = 'FacebookConnect';

function generateSafeCallback(callback) {
        return function(result) {
            if(typeof callback == 'function') {
                callback.apply(null, arguments);
            }
        }
}

module.exports = {
    initWithAppId : function(appId, callback) {
        if(!appId) return false;

        var _callback = generateSafeCallback(callback);

        return exec(_callback, _callback, service, 'initWithAppId', [{appId: appId}]);

    },

    login : function(options, callback) {
        if(!options) options = {};

        var config = {
            permissions: options.permissions || ['email'],
            appId: options.appId || ''
        };

        var _callback = generateSafeCallback(callback);

        return exec(_callback, _callback, service, 'login', [config]);

    },

    /**
     * Make an asynchrous Facebook Graph API request.
     *
     * @param {String} path Is the path to the Graph API endpoint.
     * @param {Object} [options] Are optional key-value string pairs representing the API call parameters.
     * @param {String} [httpMethod] Is an optional HTTP method that defaults to GET.
     * @param {Function} [callback] Is an optional callback method that receives the results of the API call.
     */
    requestWithGraphPath : function(path, options, httpMethod, callback) {
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

        var _callback = generateSafeCallback(callback);

        return exec(_callback, _callback, service, 'requestWithGraphPath', [{path: path, options: options, httpMethod: httpMethod}]);

    },

    logout : function(callback) {

        var _callback = generateSafeCallback(callback);

        return exec(_callback, _callback, service, 'logout', []);

    },

    dialog : function(method, options, callback) {

        var _callback = generateSafeCallback(callback);

        return exec(_callback, _callback, service, 'dialog', [{method: method, params: options}]);

    }
}
    

