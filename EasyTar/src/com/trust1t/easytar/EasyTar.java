package com.trust1t.easytar;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.trust1t.android.sdk.eid.core.CardService;
import com.trust1t.android.sdk.eid.core.constants.CardIntent;
import com.trust1t.android.sdk.eid.core.constants.Configuration;
import com.trust1t.android.sdk.eid.core.constants.ReaderStatus;
import com.trust1t.android.sdk.eid.core.files.Address;
import com.trust1t.android.sdk.eid.core.files.Identity;
import com.trust1t.android.sdk.eid.core.files.PinResult;
import com.trust1t.android.sdk.eid.core.impl.FileType;
import com.trust1t.android.sdk.eid.core.tlv.TlvParser;
import com.trust1t.easytar.javascriptbridge.ErrorMessage;
import com.trust1t.easytar.javascriptbridge.JsCertificate;
import com.trust1t.easytar.javascriptbridge.JsSignature;
import com.trust1t.easytar.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class EasyTar extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = false;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private WebView myWebView;
	private TextView statusText;
	private JavaScriptHandler javascriptHandler;

	/** card stuff **/
	// Files variables
	private Identity identity;
	private Address address;
	private byte[] photo;
	private List<X509Certificate> certs = new ArrayList<X509Certificate>();
	private PinResult result;
	
	private String pinCode;

	// Status variable
	private TextView readerStatusTextView;

	private void setReaderStatusInUi(final String readerStatus) {
		runOnUiThread(new Runnable() {
			public void run() {
				// readerStatusTextView.setText(readerStatus);
			}
		});
	}

	public Identity getIdentity() {
		return identity;
	}

	public Address getAddress() {
		return address;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public List<X509Certificate> getCertificates() {
		return certs;
	}

	public PinResult getPinResult() {
		return result;
	}

	/** ------- VARIABLES------- **/
	// Bind variables
	private CardService boundCardervice;

	public CardService getCardService() {
		return boundCardervice;
	}

	private boolean isCardServiceBound;

	// Broadcast variables
	private CardResponseReceiver receiver;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	private Button btnRefresh;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.readerStatusTextView = (TextView) findViewById(R.id.status_text);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll().penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
				.penaltyLog().build());

		setContentView(R.layout.activity_easy_tar);
		
		

		//final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
//							if (mControlsHeight == 0) {
//								mControlsHeight = controlsView.getHeight();
//							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
//							controlsView
//									.animate()
//									.translationY(visible ? 0 : mControlsHeight)
//									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
//							controlsView.setVisibility(visible ? View.VISIBLE
//									: View.GONE);
						}

					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					// mSystemUiHider.show();
				}
			}
		});

		// other UI stuff
		this.statusText = (TextView) this.findViewById(R.id.status_text);

		// webview stuff..

		// Load the webview
		myWebView = (WebView) this.findViewById(R.id.fullscreen_content);
		myWebView.setWebViewClient(new WebViewClient());
		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.getSettings().setDomStorageEnabled(true);

		// Add our javascripthandler to the webview
		javascriptHandler = new JavaScriptHandler(this);

		myWebView.addJavascriptInterface(javascriptHandler, "EasyTar");

		// Load our test url
		myWebView.loadUrl("https://www.easytar.be/Android");
		//myWebView.loadUrl("file:///android_asset/index.html");

		myWebView.setWebChromeClient(new WebChromeClient() {
			public void onConsoleMessage(String message, int lineNumber,
					String sourceID) {
				Log.d("EasyTar", message + " -- From line " + lineNumber
						+ " of " + sourceID);
			}
		});
		

		btnRefresh = (Button) findViewById(R.id.button1);
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				myWebView.loadUrl("https://www.easytar.be/Android");
			}
		});
		
		
		
	}
	
	public void requestForPin()
	{
		if(this.pinCode == null || this.pinCode.isEmpty())
		{
			askPincode();
		}
	}
	
	private void askPincode()
	{
		final EditText input = new EditText(EasyTar.this);
		
		new AlertDialog.Builder(EasyTar.this)
	    .setTitle("Enter your pincode")
	    .setMessage("canhazpin?")
	    .setView(input)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            Editable value = input.getText(); 
	            try {
					if(!value.toString().isEmpty())
					{
						EasyTar.this.pinCode = value.toString();
						boundCardervice.verifyPin(EasyTar.this.pinCode);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.i("EasyTar", "error: " + e.getMessage());
				}
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            Log.i("EasyTar", "user didnt want to enter his pin");
	        }
	    }).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerCardResponseReceiver();
		if (boundCardervice != null)
			boundCardervice.fetchReaderStatus();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}

	@Override
	protected void onStart() {
		super.onStart();
		doBindService();
	}
	
	

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	public void changeStatus(String status) {
		this.statusText.setText(status);
	}

	/** ------- BINDING METHODS ------- **/
	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		Log.i(Configuration.CARD_SERVICE_LOG_FILTER,
				"binding cardservice to main activity.");

		// TODO: test if this works as wanted
		// ComponentName service = startService(new Intent(this,
		// CardService.class));
		bindService(new Intent(this, CardService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		isCardServiceBound = true;
	}

	void doUnbindService() {
		if (isCardServiceBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			isCardServiceBound = false;
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			boundCardervice = ((CardService.LocalBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			boundCardervice = null;
		}
	};

	/** ------- BROADCAST RECEIVER METHODS ------- **/
	public class CardResponseReceiver extends BroadcastReceiver {
		public static final String INTENT_ACTION = "Action";

		@Override
		public void onReceive(Context context, final Intent intent) {
			Log.d(Configuration.CARD_SERVICE_LOG_FILTER,
					"Action received with intent is " + intent.getAction());
			if (intent.getAction().equals(
					Configuration.CARD_SERVICE_BROADCAST_ACTION)) {
				String kindOfOperation = intent
						.getStringExtra(CardIntent.CARD_OPERATION_RESPONSE);
				Log.d(Configuration.CARD_SERVICE_LOG_FILTER,
						"Received broadcast with type of response "
								+ kindOfOperation);
				if (kindOfOperation.equals(CardIntent.READER_STATUS_RESPONSE)) {
					Log.d(Configuration.CARD_SERVICE_LOG_FILTER,
							"Reader status is "
									+ intent.getStringExtra(CardIntent.READER_STATUS));

					changeStatus(intent
							.getStringExtra(CardIntent.READER_STATUS));

					// if the reader status is card found, we can read the data
					// we want to visualize
					if (intent.getStringExtra(CardIntent.READER_STATUS).equals(
							ReaderStatus.CARD_FOUND)) {
						// boundCardervice.signHash("testess", "1234");
						// boundCardervice.signAuth("testess", "1234");
						// boundCardervice.signHash("testess", "1234");
						// boundCardervice.readFileType(FileType.Identity);
						// boundCardervice.readFileType(FileType.Address);
						// boundCardervice.readFileType(FileType.Photo);
						// boundCardervice.readFileType(FileType.CACertificate);
						// boundCardervice.readFileType(FileType.NonRepudiationCertificate);
						// boundCardervice.readFileType(FileType.AuthentificationCertificate);
						// boundCardervice.readFileType(FileType.RootCertificate);
						// boundCardervice.readFileType(FileType.RRNCertificate);

						EasyTar.this.pinCode = null;
						requestForPin();
					}

					if (intent.getStringExtra(CardIntent.READER_STATUS).equals(
							ReaderStatus.CARDREADER_FOUND)) {
						onReaderConnected();
					}
					if (intent.getStringExtra(CardIntent.READER_STATUS).equals(
							ReaderStatus.NO_CARDREADER_FOUND)) {
						Log.i("EasyTar", "No CardReader Found, calling js now");
						onReaderDisconnected();
					}
				} else if (kindOfOperation
						.equals(CardIntent.FILE_READ_RESPONSE)) {
					String fileType = intent
							.getStringExtra(CardIntent.FILE_TYPE_RESPONSE);
					if (fileType.equals(FileType.Identity.name())) {

						// we got back identity, parse, if fail, do something
						// with error
						try {
							Log.i("EasyTar",
									"got back the rndata file from the EID");
							EasyTar.this.identity = TlvParser.parse(intent
									.getByteArrayExtra(CardIntent.FILE_BYTES),
									Identity.class);

							receivedRnData(identity, null);

						} catch (Exception e) {
							receivedRnData(null, new ErrorMessage(1, "failure"));
						}
						Log.d(Configuration.CARD_SERVICE_LOG_FILTER,
								"id atr is "
										+ EasyTar.this.identity
												.isMemberOfFamily());

					}
					if (fileType.equals(FileType.Address.name())) {
						EasyTar.this.address = TlvParser
								.parse(intent
										.getByteArrayExtra(CardIntent.FILE_BYTES),
										Address.class);

					}
					if (fileType.equals(FileType.Photo.name())) {
						//EasyTar.this.photo = intent
							//	.getByteArrayExtra(CardIntent.FILE_BYTES);
						//TODO: not necessary in this case
					}
					if (fileType.equals(FileType.NonRepudiationCertificate
							.name())) {

						// TODO: not necessary in this release

					}
					if (fileType.equals(FileType.RRNCertificate.name())) {

						// TODO: not necessary in this release

					}
					if (fileType.equals(FileType.CACertificate.name())) {

						receivedCACertificate(convertToX509Certificate(intent
								.getByteArrayExtra(CardIntent.FILE_BYTES)));

					}
					if (fileType.equals(FileType.RootCertificate.name())) {
						receivedRootCertificate(convertToX509Certificate(intent
								.getByteArrayExtra(CardIntent.FILE_BYTES)));
					}
					if (fileType.equals(FileType.AuthentificationCertificate
							.name())) {
						receivedAuthenticationCertificate(convertToX509Certificate(intent
								.getByteArrayExtra(CardIntent.FILE_BYTES)));
					}
				} else if (kindOfOperation.equals(CardIntent.PIN_RESPONSE)) 
				{
					
					EasyTar.this.result = intent
							.getParcelableExtra(CardIntent.PIN_RESULT);
					Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "result is "
							+ result.isSuccess() + " " + result.isBlocked()
							+ " " + result.getRetriesLeft());
					
					if(!result.isSuccess())
					{
						EasyTar.this.pinCode = null;
						if(!result.isBlocked())
						{
							showMessage("Pin was wrong, but you still have " + result.getRetriesLeft() + " triest left!");
						}
						else
						{
							showMessage("it appears as your eid has been blocked, there are no more tries left!");
						}
						
					}
					else
					{					
						showMessage("Pincode was correct!");
						onEidCardFound();
					}
				} 
				else if (kindOfOperation.equals(CardIntent.SIGN_RESPONSE)) {

					// we got a response, which came from the signing event

					if (intent.getStringExtra(CardIntent.SIGN_TYPE_RESPONSE)
							.equals(CardIntent.SIGN_AUTH)) {

						// it's an authenticationsign
			//			Log.d(Configuration.CARD_SERVICE_LOG_FILTER,
				//				"sign auth is "
					//					+ intent.getStringExtra(CardIntent.SIGNED_HASH));
						receivedAuthenticationSignatureResult(new String(Base64.encode(
								intent.getStringExtra(CardIntent.SIGNED_HASH).getBytes(), Base64.DEFAULT)), null);
						
					}

					else if (intent.getStringExtra(
							CardIntent.SIGN_TYPE_RESPONSE).equals(
							CardIntent.SIGN)) {
						// it's a non-repudiation signature
						Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "sign is "
								+ intent.getStringExtra(CardIntent.SIGNED_HASH));
					}

				} else if (kindOfOperation
						.equals(CardIntent.EXCEPTION_RESPONSE)) {
					Log.d(Configuration.CARD_SERVICE_LOG_FILTER,
							"Exception thrown while reading from card "
									+ intent.getStringExtra(CardIntent.EXCEPTION_MESSAGE));
				}
			}
		}
	}

	private void showMessage(String message)
	{
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}

	/** broadcastreceiver helper methods **/
	private X509Certificate convertToX509Certificate(byte[] certificate) {
		try {
			CertificateFactory factory = CertificateFactory
					.getInstance("X.509");
			return (X509Certificate) (factory
					.generateCertificate(new ByteArrayInputStream(certificate)));
		} catch (final CertificateException e) {
			throw new RuntimeException("X.509 algo", e);
		}
	}

	private void registerCardResponseReceiver() {
		// Register broadcast receiver
		IntentFilter filter = new IntentFilter(
				Configuration.CARD_SERVICE_BROADCAST_ACTION);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new CardResponseReceiver();
		registerReceiver(receiver, filter);
	}

	/** js-api methods **/

	private void onReaderConnected() {
		this.myWebView.loadUrl("javascript:_befa_onReaderConnectedCallback();");
	}

	private void onReaderDisconnected() {
		this.myWebView
				.loadUrl("javascript:_befa_onReaderDisconnectedCallback();");
	}

	private void onEidCardFound() {
		this.myWebView.loadUrl("javascript:_befa_onEidCardDetected(\"eid\");");
	}

	private void receivedRnData(Identity identity, ErrorMessage errorMessage) {
		if (identity != null)
			javascriptHandler.passObject("rnData", new Gson().toJson(identity));

		if (errorMessage != null)
			javascriptHandler.passObject("rnDataError",
					new Gson().toJson(errorMessage));

		this.myWebView.loadUrl("javascript:_befa_onReceivedRnDataCallback()");
	}

	private void receivedAuthenticationCertificate(X509Certificate certificate) {
		try {

			String certificateString = new String(Base64.encode(
					certificate.getEncoded(), Base64.DEFAULT));

			javascriptHandler.passObject("authenticationCertificate",
					new Gson().toJson(new JsCertificate(certificateString)));

		} catch (CertificateEncodingException e) {
			e.printStackTrace();
			Log.i("certificate error: ", "error while loading certificate");

			javascriptHandler
					.passObject("authenticationCertificateError", new Gson()
							.toJson(new ErrorMessage(1, "certificate error")));
		}

		this.myWebView
				.loadUrl("javascript:_befa_onReceivedAuthenticationCertificateCallback()");
	}

	private void receivedCACertificate(X509Certificate certificate) {
		try {

			String certificateString = new String(Base64.encode(
					certificate.getEncoded(), Base64.DEFAULT));

			javascriptHandler.passObject("citizenCertificate",
					new Gson().toJson(new JsCertificate(certificateString)));

		} catch (CertificateEncodingException e) {
			e.printStackTrace();
			Log.i("certificate error: ", "error while loading certificate");

			javascriptHandler
					.passObject("citizenCertificateError", new Gson()
							.toJson(new ErrorMessage(1, "certificate error")));
		}

		this.myWebView
				.loadUrl("javascript:_befa_onReceivedCitizenCertificateCallback()");

	}

	private void receivedRootCertificate(X509Certificate certificate) {
		try {

			String certificateString = new String(Base64.encode(
					certificate.getEncoded(), Base64.DEFAULT));

			javascriptHandler.passObject("rootCertificate",
					new Gson().toJson(new JsCertificate(certificateString)));

		} catch (CertificateEncodingException e) {
			e.printStackTrace();
			Log.i("certificate error: ", "error while loading certificate");

			javascriptHandler
					.passObject("rootCertificateError", new Gson()
							.toJson(new ErrorMessage(1, "certificate error")));
		}

		this.myWebView
				.loadUrl("javascript:_befa_onReceivedRootCertificateCallback()");

	}

	private void receivedAddress(Address address, ErrorMessage errorMessage) {
		if (address != null)
			javascriptHandler.passObject("addressData",
					new Gson().toJson(address));

		if (errorMessage != null)
			javascriptHandler.passObject("addressDataError",
					new Gson().toJson(errorMessage));

		this.myWebView
				.loadUrl("javascript:_befa_onReceivedAddressDataCallback()");
	}

	private void receivedPhoto(String photoAsBase64, ErrorMessage errorMessage) {
		if (photoAsBase64 != null && !photoAsBase64.isEmpty())
			javascriptHandler.passObject("photo",
					new Gson().toJson(photoAsBase64));

		if (errorMessage != null)
			javascriptHandler.passObject("photoError",
					new Gson().toJson(errorMessage));

		this.myWebView
				.loadUrl("javascript:_befa_onReceivedAddressDataCallback()");
	}
	
	private void receivedAuthenticationSignatureResult(String authenticationSignature, ErrorMessage errorMessage)
	{
		javascriptHandler.passObject("authenticationSignature", new Gson().toJson(new JsSignature(authenticationSignature)));
		this.myWebView.loadUrl("javascript:_befa_onReceivedAuthenticationSignatureCallback()");
	}

	public CardService getBoundCardervice() {
		return boundCardervice;
	}

	public void setBoundCardervice(CardService boundCardervice) {
		this.boundCardervice = boundCardervice;
	}

	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}
	
	

}
