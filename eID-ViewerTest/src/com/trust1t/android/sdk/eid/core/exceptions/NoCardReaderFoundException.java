package com.trust1t.android.sdk.eid.core.exceptions;

/**
 * Created by KwintenP on 27/08/13.
 */
public class NoCardReaderFoundException extends Exception{

    public NoCardReaderFoundException(){
        super();
    }

    public NoCardReaderFoundException(String msg){
        super(msg);
    }
}
