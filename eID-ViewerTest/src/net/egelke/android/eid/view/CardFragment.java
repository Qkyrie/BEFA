package net.egelke.android.eid.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.trust1t.android.sdk.eid.core.CardService;
import com.trust1t.android.sdk.eid.core.files.Identity;
import com.trust1t.android.sdk.eid.core.files.PinResult;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class CardFragment extends Fragment {
	
	private TextView cardNr;
	private TextView issuePlace;
	private TextView chipNr;
	private TextView validFrom;
	private TextView validTo;
    private Button verifyPinButton;
    private Context context;
    private CheckBox blocked;
    private CheckBox success;
    private TextView retriesLeft;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.card, container, false);
		
		cardNr = (TextView) v.findViewById(R.id.cardnr);
		issuePlace = (TextView) v.findViewById(R.id.issuePlace);
		chipNr = (TextView) v.findViewById(R.id.chipnr);
		validFrom = (TextView) v.findViewById(R.id.validFrom);
		validTo = (TextView) v.findViewById(R.id.validTo);
        verifyPinButton = (Button) v.findViewById(R.id.verifyPinButton);
        blocked = (CheckBox) v.findViewById(R.id.blocked);
        success = (CheckBox) v.findViewById(R.id.success);
        retriesLeft = (TextView) v.findViewById(R.id.retriesLeft);

        context = (MainActivity) getActivity();

        //Add click listener to verify Pin Button
        verifyPinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.prompts, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text
                                        Log.d(CardService.LOG_FILTER, "Pin entered is " + userInput.getText());
                                        ((MainActivity) getActivity()).getCardService().verifyPin(""+userInput.getText());
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });
		
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	    updateIdentity();
    }

    public void updateIdentity() {
        if (((MainActivity) getActivity()).getIdentity() != null) {
            Identity result = ((MainActivity) getActivity()).getIdentity();
            cardNr.setText(result.getCardNumber());
            issuePlace.setText(result.getCardDeliveryMunicipality());
            chipNr.setText(result.getChipNumber());
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            validFrom.setText(df.format(result.getCardValidityDateBegin().getTime()));
            validTo.setText(df.format(result.getCardValidityDateEnd().getTime()));
        }
    }

    public void updatePinResult(){
        if (((MainActivity) getActivity()).getPinResult() != null) {
            PinResult result = ((MainActivity)getActivity()).getPinResult();
            if(result.isSuccess()){
                success.setChecked(true);
                retriesLeft.setText("");
            }
            else
            {
                success.setChecked(false);
                blocked.setChecked(result.isBlocked());
                retriesLeft.setText(""+result.getRetriesLeft());
            }
        }
    }


}
