package com.trust1t.android.sdk.eid.core.constants;

/**
 * Created by KwintenP on 28/08/13.
 */
public class CardIntent {

    /** --------- Used to communicate towards the card --------- **/
    /** Card operation to be performed **/
    public static final String CARD_OPERATION = "CARD_OPERATION";
    /** Different operations **/
    public static final String READ_FILE = "READ_FILE";
    public static final String VERIFY_PIN = "VERIFY_PIN";
    public static final String SIGN = "SIGN";
    public static final String SIGN_AUTH = "SIGN_AUTH";
    /** Variable send with the operation **/
    public static final String PIN = "PIN";
    public static final String FILE_TYPE = "FILE_TYPE";
    public static final String HASH_TO_SIGN = "HASH_TO_SIGN";

    /** --------- Used to communicate back --------- **/
    /** response to card operation **/
    public static final String CARD_OPERATION_RESPONSE = "CARD_OPERATION_RESPONSE";
    /** different response types **/
    public static final String PIN_RESPONSE = "PIN_RESPONSE";
    public static final String FILE_READ_RESPONSE = "FILE_READ_RESPONSE";
    public static final String FILE_TYPE_RESPONSE = "FILE_TYPE_RESPONSE";
    public static final String EXCEPTION_RESPONSE = "EXCEPTION_RESPONSE";
    public static final String SIGN_RESPONSE = "SIGN_RESPONSE";
    public static final String SIGN_TYPE_RESPONSE = "SIGN_TYPE_RESPONSE";
    public static final String READER_STATUS_RESPONSE = "READER_STATUS_RESPONSE";
    /** Variable send with the response **/
    public static final String PIN_RESULT = "PIN_RESULT";
    public static final String FILE_BYTES = "FILE_BYTES";
    public static final String EXCEPTION_MESSAGE = "EXCEPTION_MESSAGE";
    public static final String READER_STATUS = "READER_STATUS";
    public static final String SIGNED_HASH = "SIGNED_HASH";
}
