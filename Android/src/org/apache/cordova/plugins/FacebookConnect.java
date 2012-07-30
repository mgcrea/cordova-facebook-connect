//
//  FacebookConnect.java
//
// Created by Olivier Louvignes on 2012-07-20.
//
// Copyright 2012 Olivier Louvignes. All rights reserved.
// MIT Licensed

package org.apache.cordova.plugins;

import org.apache.cordova.api.*;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;

@SuppressWarnings("deprecation")
public class FacebookConnect extends Plugin {

	private final String CLASS = "FacebookConnect";

	private String appId;
	private Facebook _facebook;
	private AuthorizeDialogListener authorizeDialogListener;
	//private final Handler handler = new Handler();

	public Facebook getFacebook() {
		if(this.appId == null) {
			Log.e(CLASS, "ERROR: You must provide a non-empty appId.");
		}
		if(this._facebook == null) {
			this._facebook = new Facebook(this.appId);
		}
		return _facebook;
	}

	@Override
	public PluginResult execute(final String action, final JSONArray args, final String callbackId) {
		PluginResult pluginResult = new PluginResult(PluginResult.Status.INVALID_ACTION, "Unsupported operation: " + action);

		try {
			if(action.equals("initWithAppId")) pluginResult = this.initWithAppId(args, callbackId);
			else if(action.equals("login")) pluginResult = this.login(args, callbackId);
			else if(action.equals("requestWithGraphPath")) pluginResult = this.requestWithGraphPath(args, callbackId);
			else if(action.equals("dialog")) pluginResult = this.dialog(args, callbackId);
			else if(action.equals("logout")) pluginResult = this.logout(args, callbackId);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			pluginResult = new PluginResult(PluginResult.Status.MALFORMED_URL_EXCEPTION);
		} catch (IOException e) {
			e.printStackTrace();
			pluginResult = new PluginResult(PluginResult.Status.IO_EXCEPTION);
		} catch (JSONException e) {
			e.printStackTrace();
			pluginResult = new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}

		return pluginResult;
	}

	/**
	 * Cordova interface to initialize the appId
	 *
	 * @param args
	 * @param callbackId
	 * @return PluginResult
	 * @throws JSONException
	 */
	public PluginResult initWithAppId(final JSONArray args, final String callbackId) throws JSONException {
		Log.d(CLASS, "initWithAppId()");
		JSONObject params = args.getJSONObject(0);
		PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
		JSONObject result = new JSONObject();

		this.appId = params.getString("appId");
		Facebook facebook = this.getFacebook();
		result.put("appId", this.appId);

		// Check for any stored session update Facebook session information
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getContext());
		String accessToken = prefs.getString("access_token", null);
		Long accessExpires = prefs.getLong("access_expires", 0);
		if(accessToken != null) facebook.setAccessToken(accessToken);
		if(accessExpires != 0) facebook.setAccessExpires(accessExpires);

		result.put("accessToken", accessToken);
		result.put("expirationDate", accessExpires);

		pluginResult = new PluginResult(PluginResult.Status.OK, result);
		this.success(pluginResult, callbackId);

		return pluginResult;

	}

	/**
	 * Cordova interface to perform a login
	 *
	 * @param args
	 * @param callbackId
	 * @return PluginResult
	 * @throws JSONException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public PluginResult login(final JSONArray args, final String callbackId) throws JSONException, MalformedURLException, IOException {
		Log.d(CLASS, "login() :" + args.toString());
		JSONObject params = args.getJSONObject(0);
		PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);

		if(params.has("appId")) this.appId = params.getString("appId");
		Facebook facebook = this.getFacebook();

		// Check for any stored session update Facebook session information
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getContext());
		String accessToken = prefs.getString("access_token", null);
		Long accessExpires = prefs.getLong("access_expires", 0);
		if(accessToken != null) facebook.setAccessToken(accessToken);
		if(accessExpires != 0) facebook.setAccessExpires(accessExpires);

		if(!this.getFacebook().isSessionValid()) {
			JSONArray permissionsArray = (JSONArray)params.get("permissions");
			final String[] permissions = new String[permissionsArray.length()];
			for (int i=0; i < permissionsArray.length(); i++) {
				permissions[i] = permissionsArray.getString(i);
			}

			final FacebookConnect me = this;
			this.authorizeDialogListener = new AuthorizeDialogListener(me, callbackId);
			this.cordova.setActivityResultCallback(this);
			Runnable runnable = new Runnable() {
				public void run() {
					me.getFacebook().authorize(me.cordova.getActivity(), permissions, me.authorizeDialogListener);
				};
			};
			pluginResult.setKeepCallback(true);
			this.cordova.getActivity().runOnUiThread(runnable);
		} else {
			JSONObject result = new JSONObject(facebook.request("/me"));
			result.put("accessToken", accessToken);
			result.put("expirationDate", accessExpires);
			Log.d(CLASS, "login::result " + result.toString());
			pluginResult = new PluginResult(PluginResult.Status.OK, result);
		}

		return pluginResult;
	}

	/**
	 * Cordova interface to perfom a graph request
	 *
	 * @param args
	 * @param callbackId
	 * @return PluginResult
	 * @throws JSONException
	 * @throws FileNotFoundException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public PluginResult requestWithGraphPath(final JSONArray args, final String callbackId) throws JSONException, FileNotFoundException, MalformedURLException, IOException {
		Log.d(CLASS, "requestWithGraphPath() :" + args.toString());
		JSONObject params = args.getJSONObject(0);
		PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);

		Facebook facebook = this.getFacebook();
		String path = params.has("path") ? params.getString("path") : "me";
		JSONObject optionsObject = (JSONObject)params.get("options");
		final Bundle options = new Bundle();
		Iterator<?> keys = optionsObject.keys();
		while( keys.hasNext() ){
			String key = (String)keys.next();
			options.putString(key, optionsObject.getString(key));
			//if(optionsObject.get(key) instanceof JSONObject)
		}
		String httpMethod = params.has("httpMethod") ? params.getString("httpMethod") : "GET";

		JSONObject result = new JSONObject(facebook.request(path, options, httpMethod));
		Log.d(CLASS, "requestWithGraphPath::result " + result.toString());
		pluginResult = new PluginResult(PluginResult.Status.OK, result);

		return pluginResult;
	}

	/**
	 * Cordova interface to display a dialog
	 *
	 * @param args
	 * @param callbackId
	 * @return PluginResult
	 * @throws JSONException
	 * @throws FileNotFoundException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public PluginResult dialog(final JSONArray args, final String callbackId) throws JSONException, FileNotFoundException, MalformedURLException, IOException {
		Log.d(CLASS, "dialog() :" + args.toString());
		JSONObject params = args.getJSONObject(0);
		PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);

		final String method = params.has("method") ? params.getString("method") : "feed";
		JSONObject optionsObject = (JSONObject)params.get("params");
		final Bundle options = new Bundle();
		Iterator<?> keys = optionsObject.keys();
		while( keys.hasNext() ){
			String key = (String)keys.next();
			options.putString(key, optionsObject.getString(key));
			//if(optionsObject.get(key) instanceof JSONObject)
		}

		final FacebookConnect me = this;
		Runnable runnable = new Runnable() {
			public void run() {
				me.getFacebook().dialog(me.cordova.getContext(), method, options, new RegularDialogListener(me, callbackId));
			};
		};
		pluginResult.setKeepCallback(true);
		this.cordova.getActivity().runOnUiThread(runnable);

		return pluginResult;
	}

	/**
	 * Cordova interface to logout from Facebook
	 *
	 * @param args
	 * @param callbackId
	 * @return PluginResult
	 * @throws JSONException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public PluginResult logout(final JSONArray args, final String callbackId) throws JSONException, MalformedURLException, IOException {
		Log.d(CLASS, "logout() :" + args.toString());
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getContext());
		prefs.edit().remove("access_expires").commit();
		prefs.edit().remove("access_token").commit();
		this.getFacebook().logout(this.cordova.getContext());
		return new PluginResult(PluginResult.Status.OK);
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
		private String callbackId;
		private FacebookConnect source;

		public AuthorizeDialogListener(FacebookConnect me, final String callbackId) {
			super();

			this.source = me;
			this.facebook = me.getFacebook();
			this.cordova = me.cordova;
			this.callbackId = callbackId;
		}

		@Override
		public void onComplete(Bundle values) {
			Log.d(CLASS, "AuthorizeDialogListener::onComplete() " + values.toString());

			// Update session information
			final String accessToken = this.facebook.getAccessToken();
			final long accessExpires = this.facebook.getAccessExpires();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.cordova.getContext());
			prefs.edit().putString("access_token", accessToken).commit();
			prefs.edit().putLong("access_expires", accessExpires).commit();

			final AuthorizeDialogListener me = this;
			Thread thread = new Thread(new Runnable() {
				public void run() {
					PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "UnknownError");
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

					me.source.success(pluginResult, callbackId);
				}
			});
			thread.start();
		}

		@Override
		public void onFacebookError(FacebookError e) {
			Log.d(CLASS, "AuthorizeDialogListener::onFacebookError() " + e.getMessage());
			JSONObject result = new JSONObject();
			try { result.put("error", 1); result.put("message", e.getMessage()); } catch (JSONException ex) {}
			this.source.error(result, this.callbackId);
		}

		@Override
		public void onError(DialogError e) {
			Log.d(CLASS, "AuthorizeDialogListener::onError() " + e.getMessage());
			JSONObject result = new JSONObject();
			try { result.put("error", 1); result.put("message", e.getMessage()); } catch (JSONException ex) {}
			this.source.error(result, this.callbackId);
		}

		@Override
		public void onCancel() {
			Log.d(CLASS, "AuthorizeDialogListener::onCancel()");
			JSONObject result = new JSONObject();
			try { result.put("cancelled", 1); } catch (JSONException e) {}
			this.source.error(result, callbackId);
		}

	}

	/**
	 * RegularDialogListener
	 */
	class RegularDialogListener implements DialogListener {

		//private Facebook facebook;
		//private CordovaInterface cordova;
		private String callbackId;
		private FacebookConnect source;

		public RegularDialogListener(FacebookConnect me, final String callbackId) {
			super();

			this.source = me;
			//this.facebook = me.getFacebook();
			//this.cordova = me.cordova;
			this.callbackId = callbackId;
		}

		@Override
		public void onComplete(Bundle values) {
			Log.d(CLASS, "RegularDialogListener::onComplete() " + values.toString());

			JSONObject result = new JSONObject();
			Iterator<?> keys = values.keySet().iterator();
			while( keys.hasNext() ){
				String key = (String)keys.next();
				try {
					result.put(key, values.get(key));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);

			pluginResult.setKeepCallback(false);
			this.source.success(pluginResult, this.callbackId);
		}

		@Override
		public void onFacebookError(FacebookError e) {
			Log.d(CLASS, "RegularDialogListener::onFacebookError() " + e.getMessage());
			JSONObject result = new JSONObject();
			try { result.put("error", 1); result.put("message", e.getMessage()); } catch (JSONException ex) {}
			this.source.error(result, this.callbackId);
		}

		@Override
		public void onError(DialogError e) {
			Log.d(CLASS, "RegularDialogListener::onError() " + e.getMessage());
			JSONObject result = new JSONObject();
			try { result.put("error", 1); result.put("message", e.getMessage()); } catch (JSONException ex) {}
			this.source.error(result, this.callbackId);
		}

		@Override
		public void onCancel() {
			Log.d(CLASS, "RegularDialogListener::onCancel()");
			JSONObject result = new JSONObject();
			try { result.put("cancelled", 1); } catch (JSONException e) {}
			this.source.error(result, this.callbackId);
		}

	}
}
