package com.trust1t.android.sdk.eid.core;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.precisebiometrics.android.mtk.api.smartcardio.CardException;
import com.trust1t.android.sdk.eid.core.components.BeIDCard;
import com.trust1t.android.sdk.eid.core.components.BeIDDigest;
import com.trust1t.android.sdk.eid.core.components.NonStopIntentService;
import com.trust1t.android.sdk.eid.core.components.PINPurpose;
import com.trust1t.android.sdk.eid.core.constants.CardIntent;
import com.trust1t.android.sdk.eid.core.constants.Configuration;
import com.trust1t.android.sdk.eid.core.files.PinResult;
import com.trust1t.android.sdk.eid.core.impl.FileType;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by KwintenP on 29/08/13.
 */
public class CardIntentService extends NonStopIntentService{


    /** -------- VARIABLES -------- **/
    /** beIDCard instance used to send APDUCommands to the card **/
    private BeIDCard beIDCard;

    public void setBeIDCard(BeIDCard beIDcard){
        this.beIDCard = beIDcard;
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Beid card set is " + beIDcard.getATR() + " " + beIDCard.getATR() );
    }

    /** binder variable to bind this instance to the cardservice **/
    private final IBinder mBinder = new LocalBinder();


    /** -------- CONSTRUCTOR -------- **/
    public CardIntentService(){
        super("CardIntentService");
    }

    /** -------- OVERRRIDEN METHODS -------- **/
    @Override
    protected void onHandleIntent(Intent intent) {
        try{
              if(beIDCard != null){
                Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Intent operation passed is "+ intent.getStringExtra(CardIntent.CARD_OPERATION));
                if(intent.getStringExtra(CardIntent.CARD_OPERATION).equals(CardIntent.READ_FILE)){
                    FileType type = FileType.valueOf(intent.getStringExtra(CardIntent.FILE_TYPE));
                    Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Reading file type " + type.name());
                    byte[] data = beIDCard.readFile(type);
                    sendFileData(data, type);
                }
                else if(intent.getStringExtra(CardIntent.CARD_OPERATION).equals(CardIntent.VERIFY_PIN)){
                    String pin =intent.getStringExtra(CardIntent.PIN);
                    Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Verifying pin");
                    PinResult result = beIDCard.verifyPin(PINPurpose.PINTest, pin.toCharArray());
                    sendPinResult(result);
                }
                else if(intent.getStringExtra(CardIntent.CARD_OPERATION).equals(CardIntent.SIGN)){
                    String hash =intent.getStringExtra(CardIntent.HASH_TO_SIGN);
                    String pin =intent.getStringExtra(CardIntent.PIN);
                    Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Received request to sign hash " + hash);
                    byte[] result = beIDCard.sign(hash.getBytes(), BeIDDigest.SHA_512, FileType.NonRepudiationCertificate,pin.toCharArray());
                    String signedHash = new String(result);
                    sendSignedHash(signedHash, CardIntent.SIGN);
                    Log.d(CardService.LOG_FILTER, "Signedhash is "+ signedHash);
                }
                else if(intent.getStringExtra(CardIntent.CARD_OPERATION).equals(CardIntent.SIGN_AUTH)){
                      String hash =intent.getStringExtra(CardIntent.HASH_TO_SIGN);
                      String pin =intent.getStringExtra(CardIntent.PIN);
                      Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Received request to auth sign " + hash);
                      byte[] result = beIDCard.signAuthn(hash.getBytes(), pin);
                      String signedHash = new String(result);
                      sendSignedHash(signedHash, CardIntent.SIGN_AUTH);
                      Log.d(CardService.LOG_FILTER, "Signedhash is "+ signedHash);
                }
            }
        }
        catch(CardException e){
            if(e.getMessage().contains("SCARD_E_NO_SMARTCARD")){
                Log.e(Configuration.CARD_SERVICE_LOG_FILTER, "Card was removed, setting card instance to null");
                beIDCard = null;
            }
            sendException(e.getMessage());
            Log.e(Configuration.CARD_SERVICE_LOG_FILTER, "CardException thrown in CardIntentService", e);
        }
        catch(IOException e){
            sendException(e.getMessage());
            Log.e(Configuration.CARD_SERVICE_LOG_FILTER, "IOException thrown in CardIntentService", e);
        }
        catch(InterruptedException e){
            sendException(e.getMessage());
            Log.e(Configuration.CARD_SERVICE_LOG_FILTER, "InterruptedException thrown in CardIntentService", e);
        }
        catch(NoSuchAlgorithmException e){
            sendException(e.getMessage());
            Log.e(Configuration.CARD_SERVICE_LOG_FILTER, "NoSuchAlgorithmException thrown in CardIntentService", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** -------- BINDER METHODS -------- **/
    public class LocalBinder extends Binder {
        public CardIntentService getService() {
            Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Cardintentservice's binder is called");
            return CardIntentService.this;
        }
    }

    /** -------- BROADCAST METHODS -------- **/
    private void sendPinResult(PinResult result){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Configuration.CARD_SERVICE_BROADCAST_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CardIntent.CARD_OPERATION_RESPONSE, CardIntent.PIN_RESPONSE);
        broadcastIntent.putExtra(CardIntent.PIN_RESULT, result);
        sendBroadcast(broadcastIntent);
    }

    private void sendFileData(byte[] data, FileType type){
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Returning filedata for  " + type);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Configuration.CARD_SERVICE_BROADCAST_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CardIntent.CARD_OPERATION_RESPONSE, CardIntent.FILE_READ_RESPONSE);
        broadcastIntent.putExtra(CardIntent.FILE_TYPE_RESPONSE, type.name());
        broadcastIntent.putExtra(CardIntent.FILE_BYTES, data);
        sendBroadcast(broadcastIntent);
        Log.d(Configuration.CARD_SERVICE_LOG_FILTER, "Broadcasted type " + type);
    }

    private void sendException(String msg){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Configuration.CARD_SERVICE_BROADCAST_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CardIntent.CARD_OPERATION_RESPONSE, CardIntent.EXCEPTION_RESPONSE);
        broadcastIntent.putExtra(CardIntent.EXCEPTION_MESSAGE, msg);
        sendBroadcast(broadcastIntent);
    }

    private void sendSignedHash(String hash, String sign_type){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Configuration.CARD_SERVICE_BROADCAST_ACTION);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CardIntent.CARD_OPERATION_RESPONSE, CardIntent.SIGN_RESPONSE);
        if(sign_type.equals(CardIntent.SIGN_AUTH))
             broadcastIntent.putExtra(CardIntent.SIGN_TYPE_RESPONSE, CardIntent.SIGN_AUTH);
        else if(sign_type.equals(CardIntent.SIGN))
             broadcastIntent.putExtra(CardIntent.SIGN_TYPE_RESPONSE, CardIntent.SIGN);
        broadcastIntent.putExtra(CardIntent.SIGNED_HASH, hash);
        sendBroadcast(broadcastIntent);
    }

}
