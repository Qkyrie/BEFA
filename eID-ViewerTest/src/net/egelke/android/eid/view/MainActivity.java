package net.egelke.android.eid.view;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import com.trust1t.android.sdk.eid.core.CardService;
import com.trust1t.android.sdk.eid.core.constants.Configuration;
import com.trust1t.android.sdk.eid.core.files.Address;
import com.trust1t.android.sdk.eid.core.files.Identity;
import com.trust1t.android.sdk.eid.core.files.PinResult;
import com.trust1t.android.sdk.eid.core.impl.FileType;
import com.trust1t.android.sdk.eid.core.constants.CardIntent;
import com.trust1t.android.sdk.eid.core.constants.ReaderStatus;
import com.trust1t.android.sdk.eid.core.tlv.TlvParser;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends Activity {

    private boolean userConnected;

    /** ------- VARIABLES------- **/
    //Bind variables
    private CardService boundCardervice;
    public CardService getCardService(){
        return boundCardervice;
    }
    private boolean isCardServiceBound;

    //Broadcast variables
    private CardResponseReceiver receiver;

    //Files variables
    private Identity identity;
    private Address address;
    private byte[] photo;
    private List<X509Certificate> certs = new ArrayList<X509Certificate>();
    private PinResult result;

    //Status variable
    private TextView readerStatusTextView;
    private void setReaderStatusInUi(final String readerStatus){
        runOnUiThread(new Runnable() {
            public void run() {
                readerStatusTextView.setText(readerStatus);
            }
        });
    }

    public Identity getIdentity(){
        return identity;
    }
    public Address getAddress(){
        return address;
    }
    public byte[] getPhoto(){
        return photo;
    }
    public List<X509Certificate> getCertificates(){
        return certs;
    }
    public PinResult getPinResult(){
        return result;
    }


    /** ------- OVERRIDDEN METHODS ------- **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_status);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());

        //GetReaderStatus
        readerStatusTextView = (TextView)findViewById(R.id.readerStatus);

        //Create the action bar
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        bar.addTab(bar.newTab().setText(R.string.identity).setTabListener(new TabListener(this, "identity", IdentityFragment.class)));
        bar.addTab(bar.newTab().setText(R.string.card).setTabListener(new TabListener(this, "card", CardFragment.class)));
        bar.addTab(bar.newTab().setText(R.string.certificates).setTabListener(new TabListener(this, "certificate", CertificateFragment.class)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
        outState.putBoolean("connected", userConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerCardResponseReceiver();
        if(boundCardervice != null)
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
    protected void onStart(){
        super.onStart();
        doBindService();
    }



    /** ------- BINDING METHODS ------- **/
    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        Log.i(Configuration.CARD_SERVICE_LOG_FILTER, "binding cardservice to main activity.");

        //TODO: test if this works as wanted
        //ComponentName service = startService(new Intent(this, CardService.class));
        bindService(new Intent(this,
                CardService.class), mConnection, Context.BIND_AUTO_CREATE);
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
            boundCardervice = ((CardService.LocalBinder)service).getService();
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
            Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Action received with intent is " + intent.getAction());
            if(intent.getAction().equals(Configuration.CARD_SERVICE_BROADCAST_ACTION)){
                String kindOfOperation = intent.getStringExtra(CardIntent.CARD_OPERATION_RESPONSE);
                Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Received broadcast with type of response " + kindOfOperation);
                if(kindOfOperation.equals(CardIntent.READER_STATUS_RESPONSE)){
                    Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Reader status is " + intent.getStringExtra(CardIntent.READER_STATUS));
                    setReaderStatusInUi(intent.getStringExtra(CardIntent.READER_STATUS));
                    //if the reader status is card found, we can read the data we want to visualize
                    if(intent.getStringExtra(CardIntent.READER_STATUS).equals(ReaderStatus.CARD_FOUND))
                    {
                        //boundCardervice.signHash("testess", "1234");
                        //boundCardervice.signAuth("testess", "1234");
                         //boundCardervice.signHash("testess", "1234");
                         boundCardervice.readFileType(FileType.Identity);
                         boundCardervice.readFileType(FileType.Address);
                         boundCardervice.readFileType(FileType.Photo);
                         boundCardervice.readFileType(FileType.CACertificate);
                         boundCardervice.readFileType(FileType.NonRepudiationCertificate);
                         boundCardervice.readFileType(FileType.AuthentificationCertificate);
                         boundCardervice.readFileType(FileType.RootCertificate);
                         boundCardervice.readFileType(FileType.RRNCertificate);
                    }
                }
                else if(kindOfOperation.equals(CardIntent.FILE_READ_RESPONSE)){
                    String fileType = intent.getStringExtra(CardIntent.FILE_TYPE_RESPONSE);
                    if(fileType.equals(FileType.Identity.name())){
                        MainActivity.this.identity = TlvParser.parse(intent.getByteArrayExtra(CardIntent.FILE_BYTES), Identity.class);
                        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "id atr is " + MainActivity.this.identity.isMemberOfFamily());
                        IdentityFragment idFrag = (IdentityFragment) getFragmentManager().findFragmentByTag("identity");
                        if (idFrag != null && !idFrag.isDetached()) {
                            idFrag.updateIdentity();
                        }
                        CardFragment cardFrag = (CardFragment) getFragmentManager().findFragmentByTag("card");
                        if (cardFrag != null && !cardFrag.isDetached()) {
                            cardFrag.updateIdentity();
                        }
                    }
                    if(fileType.equals(FileType.Address.name())){
                        MainActivity.this.address = TlvParser.parse(intent.getByteArrayExtra(CardIntent.FILE_BYTES), Address.class);
                        IdentityFragment idFrag = (IdentityFragment) getFragmentManager().findFragmentByTag("identity");
                        if (idFrag != null && !idFrag.isDetached()) {
                            idFrag.updateAddress();
                        }
                    }
                    if(fileType.equals(FileType.Photo.name())){
                        MainActivity.this.photo = intent.getByteArrayExtra(CardIntent.FILE_BYTES);
                        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "photo bytes size is " + MainActivity.this.photo.length);
                        IdentityFragment idFrag = (IdentityFragment) getFragmentManager().findFragmentByTag("identity");
                        if (idFrag != null && !idFrag.isDetached()) {
                            idFrag.updatePhoto();
                        }
                    }
                    if(fileType.equals(FileType.NonRepudiationCertificate.name())){
                        //certs.add(convertToX509Certificate(intent.getByteArrayExtra(CardIntent.FILE_BYTES)));
                        addCertificateToCertificateFragment(intent);
                    }
                    if(fileType.equals(FileType.RRNCertificate.name())){
                        certs.add(convertToX509Certificate(intent.getByteArrayExtra(CardIntent.FILE_BYTES)));
                        addCertificateToCertificateFragment(intent);
                    }
                    if(fileType.equals(FileType.CACertificate.name())){
                        certs.add(convertToX509Certificate(intent.getByteArrayExtra(CardIntent.FILE_BYTES)));
                        addCertificateToCertificateFragment(intent);
                    }
                    if(fileType.equals(FileType.RootCertificate.name())){
                        certs.add(convertToX509Certificate(intent.getByteArrayExtra(CardIntent.FILE_BYTES)));
                        addCertificateToCertificateFragment(intent);
                    }
                    if(fileType.equals(FileType.AuthentificationCertificate.name())){
                        certs.add(convertToX509Certificate(intent.getByteArrayExtra(CardIntent.FILE_BYTES)));
                        addCertificateToCertificateFragment(intent);
                    }
                }
                else if(kindOfOperation.equals(CardIntent.PIN_RESPONSE)){
                    MainActivity.this.result = intent.getParcelableExtra(CardIntent.PIN_RESULT);
                    Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "result is " + result.isSuccess() + " " + result.isBlocked() + " " + result.getRetriesLeft());
                    CardFragment cardFrag = (CardFragment) getFragmentManager().findFragmentByTag("card");
                    cardFrag.updatePinResult();
                }
                else if(kindOfOperation.equals(CardIntent.SIGN_RESPONSE)){
                    if(intent.getStringExtra(CardIntent.SIGN_TYPE_RESPONSE).equals(CardIntent.SIGN_AUTH))
                        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "sign auth is " + intent.getStringExtra(CardIntent.SIGNED_HASH));
                    else if(intent.getStringExtra(CardIntent.SIGN_TYPE_RESPONSE).equals(CardIntent.SIGN))
                        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "sign is " + intent.getStringExtra(CardIntent.SIGNED_HASH));
                }
                else if(kindOfOperation.equals(CardIntent.EXCEPTION_RESPONSE)){
                    Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Exception thrown while reading from card " + intent.getStringExtra(CardIntent.EXCEPTION_MESSAGE));
                }
            }
        }
    }

    private void registerCardResponseReceiver(){
        //Register broadcast receiver
        IntentFilter filter = new IntentFilter(Configuration.CARD_SERVICE_BROADCAST_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new CardResponseReceiver();
        registerReceiver(receiver, filter);
    }

    /** broadcastreceiver helper methods **/
    private X509Certificate convertToX509Certificate(byte[] certificate){
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate)(factory.generateCertificate(new ByteArrayInputStream(certificate)));
        } catch (final CertificateException e) {
            throw new RuntimeException("X.509 algo", e);
        }
    }

    public void addCertificateToCertificateFragment(Intent intent){
        CertificateFragment certFrag = (CertificateFragment) getFragmentManager().findFragmentByTag("certificate");
        if (certFrag != null && !certFrag.isDetached()) {
            List<X509Certificate> certs = new ArrayList<X509Certificate>();
            certs.add(convertToX509Certificate(intent.getByteArrayExtra(CardIntent.FILE_BYTES)));
            certFrag.addCertificates(certs);
        }
    }

    /** ------- INNER CLASSES ------- **/
    public static class TabListener implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<? extends Fragment> mClass;
        private Fragment mFragment;


        public TabListener(Activity activity, String tag, Class<? extends Fragment> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), null);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {

        }
    }
}
