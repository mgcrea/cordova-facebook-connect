//
//  FacebookConnect.java
//
// Created by Olivier Louvignes on 2012-07-20.
//
// Copyright 2012 Olivier Louvignes. All rights reserved.
// MIT Licensed

package org.apache.cordova.plugins.FacebookConnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;

@SuppressWarnings("deprecation")
public class FacebookConnect extends CordovaPlugin {

    private final String CLASS = "FacebookConnect";

    private String appId;
    private Facebook _facebook;
    private AuthorizeDialogListener authorizeDialogListener;
    //private final Handler handler = new Handler();

    public Facebook getFacebook() {
        if (this.appId == null) {
            Log.e(CLASS, "ERROR: You must provide a non-empty appId.");
        }
        if (this._facebook == null) {
            this._facebook = new Facebook(this.appId);
        }
        return _facebook;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Boolean success = false;

        try {
            if (action.equals("initWithAppId")) {
                success = this.initWithAppId(args, callbackContext);
            } else if (action.equals("login")) {
                success = this.login(args, callbackContext);
            } else if (action.equals("requestWithGraphPath")) {
                success = this.requestWithGraphPath(args, callbackContext);
            } else if (action.equals("dialog")) {
                success = this.dialog(args, callbackContext);
            } else if (action.equals("logout")) {
                success = this.logout(args, callbackContext);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.MALFORMED_URL_EXCEPTION));
        } catch (IOException e) {
            e.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION));
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
        }

        return success;
    }

    /**
     * Cordova interface to initialize the appId
     *
     * @param args JSONArray
     * @param context CallbackContext
     * @return PluginResult
     * @throws JSONException
     */
    public Boolean initWithAppId(final JSONArray args, CallbackContext context) throws JSONException {
        Log.d(CLASS, "initWithAppId()");
        JSONObject params = args.getJSONObject(0);

        JSONObject result = new JSONObject();

        this.appId = params.getString("appId");
        Facebook facebook = this.getFacebook();
        result.put("appId", this.appId);

        // Check for any stored session update Facebook session information
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity());
        String accessToken = prefs.getString("access_token", null);
        Long accessExpires = prefs.getLong("access_expires", 0);
        if (accessToken != null) {
            facebook.setAccessToken(accessToken);
        }
        if (accessExpires != 0) {
            facebook.setAccessExpires(accessExpires);
        }

        result.put("accessToken", accessToken);
        result.put("expirationDate", accessExpires);

        context.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));


        return true;

    }

    /**
     * Cordova interface to perform a login
     *
     * @param args JSONArray
     * @param context CallbackContext
     * @return PluginResult
     * @throws JSONException
     * @throws MalformedURLException
     * @throws IOException
     */
    public Boolean login(final JSONArray args, final CallbackContext context) throws JSONException, IOException {
        Log.d(CLASS, "login() :" + args.toString());
        final JSONObject params = args.getJSONObject(0);
        final PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);

        if (params.has("appId")) this.appId = params.getString("appId");
        final Facebook facebook = this.getFacebook();

        // Check for any stored session update Facebook session information
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity());
        final String accessToken = prefs.getString("access_token", null);
        final Long accessExpires = prefs.getLong("access_expires", 0);

        final FacebookConnect me = this;

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {

                if (accessToken != null) {
                    facebook.setAccessToken(accessToken);
                }
                if (accessExpires != 0) {
                    facebook.setAccessExpires(accessExpires);
                }

                if (!me.getFacebook().isSessionValid()) {
                    JSONArray permissionsArray = null;
                    String[] permissions = new String[0];

                    try {
                        permissionsArray = (JSONArray) params.get("permissions");
                        permissions = new String[permissionsArray.length()];
            for (int i = 0; i < permissionsArray.length(); i++) {
                permissions[i] = permissionsArray.getString(i);
            }
                    } catch (JSONException e) {
                        context.error("JSON exception: " + e.getMessage());
                        e.printStackTrace();
                    }

                    final String[] finalPermissions = permissions;

                    me.authorizeDialogListener = new AuthorizeDialogListener(me, context);
                    me.cordova.setActivityResultCallback(me);
            Runnable runnable = new Runnable() {
                public void run() {
                            me.getFacebook().authorize(me.cordova.getActivity(), finalPermissions, me.authorizeDialogListener);
                }
            };
            pluginResult.setKeepCallback(true);
                    me.cordova.getActivity().runOnUiThread(runnable);
        } else {
                    JSONObject result = null;
                    try {
                        result = new JSONObject(facebook.request("/me"));
            result.put("accessToken", accessToken);
            result.put("expirationDate", accessExpires);
            Log.d(CLASS, "login::result " + result.toString());
            context.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                    } catch (JSONException e) {
                        context.error("JSON exception: " + e.getMessage());
                        e.printStackTrace();
                    } catch (IOException e) {
                        context.error("IO exception: " + e.getMessage());
                        e.printStackTrace();
                    }
        }
        }
        });

        return true;
    }

    /**
     * Cordova interface to perfom a graph request
     *
     * @param args JSONArray
     * @param context CallbackContext
     * @return Boolean
     * @throws JSONException
     */
    public Boolean requestWithGraphPath(final JSONArray args, final CallbackContext context) throws JSONException {
        Log.d(CLASS, "requestWithGraphPath() :" + args.toString());
        JSONObject params = args.getJSONObject(0);

        final Facebook facebook = this.getFacebook();
        final String path = params.has("path") ? params.getString("path") : "me";
        JSONObject optionsObject = (JSONObject) params.get("options");
        final Bundle options = new Bundle();
        Iterator<?> keys = optionsObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            options.putString(key, optionsObject.getString(key));
            //if(optionsObject.get(key) instanceof JSONObject)
        }
        final String httpMethod = params.has("httpMethod") ? params.getString("httpMethod") : "GET";

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {

                try {
        JSONObject result = new JSONObject(facebook.request(path, options, httpMethod));
        Log.d(CLASS, "requestWithGraphPath::result " + result.toString());
        context.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                } catch (JSONException e) {
                    context.error("JSON exception: " + e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    context.error("IO exception: " + e.getMessage());
                    e.printStackTrace();
                }

            }
        });

        return true;
    }

    /**
     * Cordova interface to display a dialog
     *
     * @param args JSONArray
     * @param context CallbackContext
     * @return Boolean
     * @throws JSONException
     * @throws FileNotFoundException
     * @throws MalformedURLException
     * @throws IOException
     */
    public Boolean dialog(final JSONArray args, final CallbackContext context) throws JSONException, IOException {
        Log.d(CLASS, "dialog() :" + args.toString());
        JSONObject params = args.getJSONObject(0);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);

        final String method = params.has("method") ? params.getString("method") : "feed";
        JSONObject optionsObject = (JSONObject) params.get("params");
        final Bundle options = new Bundle();
        Iterator<?> keys = optionsObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            options.putString(key, optionsObject.getString(key));
            //if(optionsObject.get(key) instanceof JSONObject)
        }

        final FacebookConnect me = this;
        Runnable runnable = new Runnable() {
            public void run() {
                me.getFacebook().dialog(me.cordova.getActivity(), method, options, new RegularDialogListener(me, context));
            }
        };
        pluginResult.setKeepCallback(true);
        this.cordova.getActivity().runOnUiThread(runnable);

        return true;
    }

    /**
     * Cordova interface to logout from Facebook
     *
     * @param args JSONArray
     * @param context CallbackContext
     * @return Boolean
     * @throws JSONException
     * @throws MalformedURLException
     * @throws IOException
     */
    public Boolean logout(final JSONArray args, CallbackContext context) throws JSONException, MalformedURLException, IOException {
        Log.d(CLASS, "logout() :" + args.toString());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity());
        prefs.edit().remove("access_expires").commit();
        prefs.edit().remove("access_token").commit();
        this.getFacebook().logout(this.cordova.getActivity());
        context.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.getFacebook().authorizeCallback(requestCode, resultCode, data);
        //this.webView.sendJavascript("window.alert('test')"); //@todo not working :(
    }

    /**
     * RegularDialogListener
     */
    class AuthorizeDialogListener implements DialogListener {

        private Facebook facebook;
        private CordovaInterface cordova;
        private CallbackContext context;
        private FacebookConnect source;

        public AuthorizeDialogListener(FacebookConnect me, CallbackContext context) {
            super();

            this.source = me;
            this.facebook = me.getFacebook();
            this.cordova = me.cordova;
            this.context = context;
        }

        @Override
        public void onComplete(Bundle values) {
            Log.d(CLASS, "AuthorizeDialogListener::onComplete() " + values.toString());

            // Update session information
            final String accessToken = this.facebook.getAccessToken();
            final long accessExpires = this.facebook.getAccessExpires();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getActivity());
            prefs.edit().putString("access_token", accessToken).commit();
            prefs.edit().putLong("access_expires", accessExpires).commit();

            final AuthorizeDialogListener me = this;
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    PluginResult pluginResult;
                    try {
                        JSONObject result = new JSONObject(me.facebook.request("/me"));
                        result.put("accessToken", accessToken);
                        result.put("expirationDate", accessExpires);
                        Log.d(CLASS, "AuthorizeDialogListener::result " + result.toString());
                        pluginResult = new PluginResult(PluginResult.Status.OK, result);
                    } catch (MalformedURLException e) {
                        pluginResult = new PluginResult(PluginResult.Status.ERROR, "MalformedURLException");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        pluginResult = new PluginResult(PluginResult.Status.ERROR, "JSONException");
                        e.printStackTrace();
                    } catch (IOException e) {
                        pluginResult = new PluginResult(PluginResult.Status.ERROR, "JSONException");
                        e.printStackTrace();
                    }

                    me.context.sendPluginResult(pluginResult);
                }
            });
            thread.start();
        }

        @Override
        public void onFacebookError(FacebookError e) {
            Log.d(CLASS, "AuthorizeDialogListener::onFacebookError() " + e.getMessage());
            JSONObject result = new JSONObject();
            try {
                result.put("error", 1);
                result.put("message", e.getMessage());
            } catch (JSONException ex) {
            }
            this.context.error(result);
        }

        @Override
        public void onError(DialogError e) {
            Log.d(CLASS, "AuthorizeDialogListener::onError() " + e.getMessage());
            JSONObject result = new JSONObject();
            try {
                result.put("error", 1);
                result.put("message", e.getMessage());
            } catch (JSONException ex) {
            }
            this.context.error(result);
        }

        @Override
        public void onCancel() {
            Log.d(CLASS, "AuthorizeDialogListener::onCancel()");
            JSONObject result = new JSONObject();
            try {
                result.put("cancelled", 1);
            } catch (JSONException e) {
            }
            this.context.error(result);
        }

    }

    /**
     * RegularDialogListener
     */
    class RegularDialogListener implements DialogListener {

        //private Facebook facebook;
        //private CordovaInterface cordova;
        private CallbackContext context;
        private FacebookConnect source;

        public RegularDialogListener(FacebookConnect me, CallbackContext context) {
            super();

            this.source = me;
            //this.facebook = me.getFacebook();
            //this.cordova = me.cordova;
            this.context = context;
        }

        @Override
        public void onComplete(Bundle values) {
            Log.d(CLASS, "RegularDialogListener::onComplete() " + values.toString());

            JSONObject result = new JSONObject();
            Iterator<?> keys = values.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    result.put(key, values.get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);

            pluginResult.setKeepCallback(false);
            this.context.sendPluginResult(pluginResult);
        }

        @Override
        public void onFacebookError(FacebookError e) {
            Log.d(CLASS, "RegularDialogListener::onFacebookError() " + e.getMessage());
            JSONObject result = new JSONObject();
            try {
                result.put("error", 1);
                result.put("message", e.getMessage());
            } catch (JSONException ex) {
            }
            this.context.error(result);
        }

        @Override
        public void onError(DialogError e) {
            Log.d(CLASS, "RegularDialogListener::onError() " + e.getMessage());
            JSONObject result = new JSONObject();
            try {
                result.put("error", 1);
                result.put("message", e.getMessage());
            } catch (JSONException ex) {
            }
            this.context.error(result);
        }

        @Override
        public void onCancel() {
            Log.d(CLASS, "RegularDialogListener::onCancel()");
            JSONObject result = new JSONObject();
            try {
                result.put("cancelled", 1);
            } catch (JSONException e) {
            }
            this.context.error(result);
        }

    }
}
