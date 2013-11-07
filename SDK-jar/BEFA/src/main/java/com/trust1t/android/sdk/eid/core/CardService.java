package com.trust1t.android.sdk.eid.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.precisebiometrics.android.mtk.api.PBInitializedCallback;
import com.precisebiometrics.android.mtk.api.mtksmartcardio.MTKTerminals;
import com.precisebiometrics.android.mtk.api.smartcardio.Card;
import com.precisebiometrics.android.mtk.api.smartcardio.CardChannel;
import com.precisebiometrics.android.mtk.api.smartcardio.CardException;
import com.precisebiometrics.android.mtk.api.smartcardio.CardTerminal;
import com.precisebiometrics.android.mtk.api.smartcardio.CardTerminals;
import com.precisebiometrics.android.mtk.api.smartcardio.TerminalFactory;
import com.trust1t.android.sdk.eid.core.components.BeIDCard;
import com.trust1t.android.sdk.eid.core.constants.CardIntent;
import com.trust1t.android.sdk.eid.core.constants.Configuration;
import com.trust1t.android.sdk.eid.core.constants.ReaderStatus;
import com.trust1t.android.sdk.eid.core.impl.FileType;

import java.util.List;

/**
 * Created by KwintenP on 27/08/13.
 */
public class CardService extends Service implements PBInitializedCallback {

    public static final String INTENTTYPE = "INTENT_TYPE";
    public static final String FILETYPE = "FILETYPE";

    //TODO: move to const class
    public static final String LOG_FILTER = "CARD_INTENT_SERVICE";


    public static final String ACTION = "action";

    // SmartCardIO objects
    private TerminalFactory factory = null;
    private CardTerminals terminals = null;
    private List<CardTerminal> terminalList = null;
    private CardTerminal terminal = null;
    private Card card = null;
    private CardChannel channel = null;

    private BeIDCard beIDCard;

    /** Thread variables **/
    /** Flag used to cancel the work thread */
    private boolean canceled = false;
    /** Holds the connection state with the API */
    private boolean scConnected = false;
    /** Flag is thread is started */
    private boolean listenerThreadStarted = false;
    private boolean cardReaderConnected = false;
    private Thread listenerThread;


    private String readerStatus = "";

    /** binding variables **/
    private CardIntentService mBoundService;
    private boolean mIsBound;
    private final IBinder mBinder = new LocalBinder();

    /** broadcastreceiver variable **/
    private UsbDetachReceiver receiver;


    /** ---------- OVERRIDDEN METHODS  ---------- **/
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Creating CardService");
        MTKTerminals.initialize(getApplicationContext(),
                this);
        doBindService();
        //We need to register our usbdetachreceiver as broadcastlistener
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        receiver = new UsbDetachReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy(){
        killThread();
        doUnbindService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    
    public void initializationFailed() {
        //TODO: handle failure
        scConnected = false;
        // Cancel and exit the work thread
        killThread();
    }


    public void initialized() {
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Initialized the scConnection");
        scConnected = true;
        // Launch the working thread that will do smart card
        spawnThread();
    }

    public void uninitialized() {
        //TODO: handle event
        scConnected = false;
        // Cancel and exit the work thread
        killThread();
    }


    /** ---------- CARD LISTENER THREAD METHODS  ---------- **/
    /**
     * Helper method close an active work thread
     */
    private synchronized void killThread() {

        // Notify the thread that it is time to exit
        canceled = true;

        if (listenerThread == null) {
            // No thread exist
            return;
        }
        if (!listenerThread.isAlive()) {
            // Old thread that is already terminated
            return;
        }

        try {
            // Wait for the thread to exit before returning
            listenerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates and starts a new work thread
     */
    private synchronized void spawnThread() {

        // The thread doing the heavy lifting
        listenerThread = new Thread() {

            public void run() {

                try {
                    // 1. Check if a card reader is connected if not, we keep polling, if so we continue
                    do{
                        // Get the default factory
                        factory = TerminalFactory.getDefault();
                        // Get the terminals for the selected factory
                        terminals = factory.terminals();
                        // Get the list of currently connected terminals
                        terminalList = terminals.list();
                        if (terminalList.isEmpty()) {
                            Log.v(Configuration.CARD_SERVICE_LOG_FILTER, "No card reader found");
                            listenerThreadStarted = true;
                            if(!readerStatus.equals(ReaderStatus.NO_CARDREADER_FOUND))
                                setReaderStatus(ReaderStatus.NO_CARDREADER_FOUND);
                            Thread.sleep(10);
                        }
                    }
                    while(terminalList.isEmpty());
                    // Use the first found terminal in the list
                    terminal = terminalList.get(0);
                    Log.d(Configuration.CARD_SERVICE_LOG_FILTER, " terminal name is " + terminal.getName());
                    cardReaderConnected = true;
                    // Run the outer loop as long as the thread isn't canceled,
                    // as long as the connection with the API is intact or until
                    // something goes unexpectedly wrong (like the smart card
                    // reader is disconnected)
                    while (scConnected && !canceled) {
                        // Test if there is a smart card in the reader
                        if (terminal.isCardPresent() == false) {
                            Log.v(Configuration.CARD_SERVICE_LOG_FILTER, "No card found");
                            if(!readerStatus.equals(ReaderStatus.CARDREADER_FOUND))
                                setReaderStatus(ReaderStatus.CARDREADER_FOUND);
                            // Poll the slot constants regularly so that we have a
                            // chance to exit the thread if it is canceled
                            listenerThreadStarted = true;
                            while (!terminal.isCardPresent() && !canceled) {
                                // Wait for a card insertion event
                                terminal.waitForCardPresent(750);
                            }

                            // Exit thread if something has canceled it
                            if (canceled) {
                                return;
                            }
                        }

                        try {
                            Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Card found");
                            listenerThreadStarted = true;
                            Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "listenerThreadStarted " + listenerThreadStarted);
                            // Connect to the smart card with either T0 or T1
                            CardService.this.card = terminal.connect("*");
                            Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Creating beIdCard");
                            CardService.this.beIDCard = new BeIDCard(CardService.this.card);
                            if(!readerStatus.equals(ReaderStatus.CARD_FOUND))
                                setReaderStatus(ReaderStatus.CARD_FOUND);
                        } catch (CardException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Check if we have been canceled
                        if (canceled) {
                            return;
                        }
                        // Poll the slot constants regularly so that we have a
                        // chance to exit the thread if it is canceled
                        while (terminal.isCardPresent() == true && !canceled) {
                            // Wait for the card to be removed.
                            terminal.waitForCardAbsent(750);
                            Log.v(Configuration.CARD_SERVICE_LOG_FILTER, "Wait for card absent....");
                        }
                    }
                    return;
                }
                // Catch more unexpected exceptions in the outer loop.
                catch (CardException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        // Reset the cancel flag
        canceled = false;
        // Start the work thread
        listenerThread.start();
    }


    /** ---------- BROADCASTING METHODS  ---------- **/
    private void sendReaderStatus(){
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Returning readerstatus " + readerStatus);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Configuration.CARD_SERVICE_BROADCAST_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CardIntent.CARD_OPERATION_RESPONSE, CardIntent.READER_STATUS_RESPONSE);
        broadcastIntent.putExtra(CardIntent.READER_STATUS, readerStatus);
        sendBroadcast(broadcastIntent);
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Broadcasted constants " + readerStatus);
    }

    private void setReaderStatus(String readerStatus){
        Log.i(Configuration.CARD_SERVICE_BROADCAST_ACTION, Thread.currentThread().getStackTrace().toString());
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Setting readerstatus to " + readerStatus);
        this.readerStatus = readerStatus;
        sendReaderStatus();
    }

    private void sendException(String msg){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Configuration.CARD_SERVICE_BROADCAST_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CardIntent.CARD_OPERATION_RESPONSE, CardIntent.EXCEPTION_RESPONSE);
        broadcastIntent.putExtra(CardIntent.EXCEPTION_MESSAGE, msg);
        sendBroadcast(broadcastIntent);
    }

    //TODO: verify if card exist, if not, throw exception
    /** ---------- SERVICE METHODS  ---------- **/
    public void verifyPin(String pin) {
        if(beIDCard!=null){
            mBoundService.setBeIDCard(beIDCard);
            Intent intent = new Intent(this, CardIntentService.class);
            intent.putExtra(CardIntent.CARD_OPERATION, CardIntent.VERIFY_PIN);
            intent.putExtra(CardIntent.PIN, pin);
            startService(intent);
        }
        else{
            sendException("Card was removed");
        }
    }

    public void readFileType(FileType type){
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Received request to read file " + type.name());
        mBoundService.setBeIDCard(beIDCard);
        Intent intent = new Intent(this, CardIntentService.class);
        intent.putExtra(CardIntent.CARD_OPERATION, CardIntent.READ_FILE);
        intent.putExtra(CardIntent.FILE_TYPE, type.name());
        startService(intent);
    }

    public void signHash(String hash, String pin){
        mBoundService.setBeIDCard(beIDCard);
        Intent intent = new Intent(this, CardIntentService.class);
        intent.putExtra(CardIntent.CARD_OPERATION, CardIntent.SIGN);
        intent.putExtra(CardIntent.PIN, pin);
        intent.putExtra(CardIntent.HASH_TO_SIGN, hash);
        startService(intent);
    }

    public void signAuth(byte[] hash, String pin){
        mBoundService.setBeIDCard(beIDCard);
        Log.i("CardService", "signing AUTH method");
        Intent intent = new Intent(this, CardIntentService.class);
        intent.putExtra(CardIntent.CARD_OPERATION, CardIntent.SIGN_AUTH);
        intent.putExtra(CardIntent.PIN, pin);
        intent.putExtra(CardIntent.HASH_TO_SIGN, hash);
        startService(intent);
    }

    public void fetchReaderStatus(){
        sendReaderStatus();
    }

    /** ---------- BINDER METHODS  ---------- **/
    /**
     * Binds this instance to the requesting activity
     */
    public class LocalBinder extends Binder {
        public CardService getService() {
            Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "CardService binder called");
            return CardService.this;
        }
    }

    /**
     * Methods responsible for binding the cardintentservice to this instance
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((CardIntentService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    void doBindService() {
        //ComponentName cardIntentService = startService(new Intent(this, CardIntentService.class));
        bindService(new Intent(this,
                CardIntentService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "After binding is CardintentService is " + mBoundService);
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /** ---------- BROADCASTRECEIVER METHOD  ---------- **/
    private class UsbDetachReceiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED))
                if(cardReaderConnected){
                    //If the cardReaderConnected was true, the Thread becomes useless
                    //We kill it and restart it so polling for the cardreader can begin
                    killThread();
                    spawnThread();
                }
        }
    }
}
