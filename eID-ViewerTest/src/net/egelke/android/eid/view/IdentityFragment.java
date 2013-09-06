package net.egelke.android.eid.view;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.datatype.DatatypeConstants;

import com.trust1t.android.sdk.eid.core.files.Identity;
import com.trust1t.android.sdk.eid.core.files.SpecialStatus;
import com.trust1t.android.sdk.eid.core.files.Address;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class IdentityFragment extends Fragment {
	
	private TextView type;

	private TextView name;

	private TextView gNames;

	private TextView birthPlace;

	private TextView birthDate;

	private TextView sex;

	private TextView natNumber;

	private TextView nationality;

	private TextView title;

	private CheckBox status_whiteCane;

	private CheckBox status_yellowCane;

	private CheckBox status_extMinority;

	private TextView street;

	private TextView zip;

	private TextView municipality;

	private ImageView photo;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.identity, container, false);
		
		type = (TextView) v.findViewById(R.id.idType);
		name = (TextView) v.findViewById(R.id.name);
		gNames = (TextView) v.findViewById(R.id.gNames);
		birthPlace = (TextView) v.findViewById(R.id.birthPlace);
		birthDate = (TextView) v.findViewById(R.id.birthDate);
		sex = (TextView) v.findViewById(R.id.sex);
		natNumber = (TextView) v.findViewById(R.id.natNumber);
		nationality = (TextView) v.findViewById(R.id.nationality);
		title = (TextView) v.findViewById(R.id.title);
		status_whiteCane = (CheckBox) v.findViewById(R.id.status_whiteCane);
		status_yellowCane = (CheckBox) v.findViewById(R.id.status_yellowCane);
		status_extMinority = (CheckBox) v.findViewById(R.id.status_extMinority);
		street = (TextView) v.findViewById(R.id.street);
		zip = (TextView) v.findViewById(R.id.zip);
		municipality = (TextView) v.findViewById(R.id.municipality);
		photo = (ImageView) v.findViewById(R.id.photo);
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
        updateIdentity();
        updateAddress();
        updatePhoto();
    }

    void updateIdentity() {
        if(((MainActivity) getActivity()).getIdentity() != null){
            Identity result = ((MainActivity)getActivity()).getIdentity();
            switch (result.getDocumentType()) {
                case BELGIAN_CITIZEN:
                    type.setText(R.string.cardtype_citizen);
                    break;
                case KIDS_CARD:
                    type.setText(R.string.cardtype_kids);
                    break;
                default:
                    type.setText(result.getDocumentType().name().replace('_', ' '));
                    break;
            }
            name.setText(result.getName());
            gNames.setText(result.getFirstName() + " " + result.getMiddleName());
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            birthPlace.setText(result.getPlaceOfBirth());
            birthDate.setText(format.format(result.getDateOfBirth().getTime()));
            switch (result.getGender()) {
                case MALE:
                    sex.setText(R.string.sex_male);
                    break;
                case FEMALE:
                    sex.setText(R.string.sex_female);
                    break;
            }
            natNumber.setText(result.getNationalNumber());
            nationality.setText(result.getNationality());
            title.setText(result.getNobleCondition());
            status_whiteCane.setChecked(false);
            status_yellowCane.setChecked(false);
            status_extMinority.setChecked(false);
            status_whiteCane.setChecked(result.getSpecialStatus().hasWhiteCane());
            status_yellowCane.setChecked(result.getSpecialStatus().hasYellowCane());
            status_extMinority.setChecked(result.getSpecialStatus().hasExtendedMinority());
        }
    }

    void updateAddress(){
        if (((MainActivity) getActivity()).getAddress() != null) {
            Address result = ((MainActivity) getActivity()).getAddress();
            street.setText(result.getStreetAndNumber());
            zip.setText(result.getZip());
            municipality.setText(result.getMunicipality());
        }
    }

    void updatePhoto(){
        if (((MainActivity) getActivity()).getPhoto() != null) {
            byte[] data = ((MainActivity)getActivity()).getPhoto();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            photo.setImageBitmap(bitmap);
        }
    }
}
