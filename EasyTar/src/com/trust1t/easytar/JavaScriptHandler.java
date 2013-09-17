package com.trust1t.easytar;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.trust1t.android.sdk.eid.core.impl.FileType;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JavaScriptHandler {

	HashMap<String, JSONObject> mObjectsFromJS = new HashMap<String, JSONObject>();

	private final EasyTar parentActivity;

	public JavaScriptHandler(EasyTar activity) {
		parentActivity = activity;
	}

	/**
	 * request to get the rndata
	 */
	@JavascriptInterface
	public void getRnData()
	{
		Log.i("JSH", "request to get RnData");
		parentActivity.getBoundCardervice().readFileType(FileType.Identity);
	}
	
	@JavascriptInterface
	public void getAddressData()
	{
		Log.i("JSH", "request to get RnData");
		parentActivity.getBoundCardervice().readFileType(FileType.Address);
	}
	
	@JavascriptInterface
	public void getRootCertificate()
	{
		Log.i("JSH", "request to get root certificate");
		parentActivity.getBoundCardervice().readFileType(FileType.RootCertificate);
	}
	
	@JavascriptInterface
	public void getCitizenCertificate()
	{
		Log.i("JSH", "request to get Citizen certificate");
		parentActivity.getBoundCardervice().readFileType(FileType.CACertificate);
	}
	
	@JavascriptInterface
	public void getAuthenticationCertificate()
	{
		Log.i("JSH", "request to get Authentication Certificate");
		parentActivity.getBoundCardervice().readFileType(FileType.AuthentificationCertificate);
	}
	@JavascriptInterface
	public void authenticationSign(String signThis)
	{
		Log.i("JSH", "we've been asked to sign " + signThis);
		parentActivity.getBoundCardervice().signAuth(signThis, parentActivity.getPinCode());
	}
	
	public void nonRepudiationSign(String signThis)
	{
		Log.i("JSH", "we've been asked to sign " + signThis);
		parentActivity.getBoundCardervice().signHash(signThis, parentActivity.getPinCode());
	}


	@JavascriptInterface
	public void passObject(String name, String json) {
		try {
			Log.i("JSH", "putting in " + json);
			
			
			mObjectsFromJS.put(name, new JSONObject(json));
		} catch (JSONException e) {
			Log.i("JSH", "error converting json: " + json);
		}
	}

	@JavascriptInterface
	public String returnObject(String name) {
		if (mObjectsFromJS.containsKey(name)) {
			
			Log.i("JSH", "returning " + name);
			
			return mObjectsFromJS.get(name).toString();
		} else {
			
			Log.i("JSH", "no value with name " + name + " was found. Current size is " + mObjectsFromJS.size());
			return "null";
		}
	}
	
	
}
