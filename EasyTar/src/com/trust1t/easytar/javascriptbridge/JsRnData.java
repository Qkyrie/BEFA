package com.trust1t.easytar.javascriptbridge;

import java.net.IDN;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import com.trust1t.android.sdk.eid.core.files.Identity;

public class JsRnData {

	private String cardNumber;
	private String deliveryMunicipality;
	private String startDate;
	private String endDate;
	private String name;
	private String firstName;
	private String altFirstName;
	private String thirdNameInitial;
	private String nationality;
	private String birthLocation;
	private String birthDate;
	private String gender;
	private String nobleCondition;
	private String specialStatus;
	private String photoHash;

	private DateFormat formatter;

	/**
	 * construct a jsrndata object from an identity
	 * 
	 * @param identity
	 */
	public JsRnData(Identity identity)
	{
		formatter = new SimpleDateFormat("yyyyMMdd");
		this.cardNumber = identity.getCardNumber();
		this.deliveryMunicipality = identity.getCardDeliveryMunicipality();
		this.altFirstName = identity.getMiddleName();
		this.name = identity.getName();
		this.firstName = identity.getFirstName();
		this.birthLocation = identity.getPlaceOfBirth();
		this.gender = identity.getGender().name();
		this.nationality = identity.getNationality();
		this.nobleCondition = identity.getNobleCondition();
		this.specialStatus = identity.getSpecialStatus().name();
		
		//dates with extra checks
		this.setEndDate(identity.getCardValidityDateEnd());
		this.setStartDate(identity.getCardValidityDateBegin());
		this.setBirthDate(identity.getDateOfBirth());
		
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getDeliveryMunicipality() {
		return deliveryMunicipality;
	}

	public void setDeliveryMunicipality(String deliveryMunicipality) {
		this.deliveryMunicipality = deliveryMunicipality;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setStartDate(GregorianCalendar startDate) {

		if (startDate != null) {
			this.startDate = formatter.format(startDate.getTime());
		}
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public void setEndDate(GregorianCalendar endDate) {
		if (endDate != null) {
			this.endDate = formatter.format(endDate.getTime());
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getAltFirstName() {
		return altFirstName;
	}

	public void setAltFirstName(String altFirstName) {
		this.altFirstName = altFirstName;
	}

	public String getThirdNameInitial() {
		return thirdNameInitial;
	}

	public void setThirdNameInitial(String thirdNameInitial) {
		this.thirdNameInitial = thirdNameInitial;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getBirthLocation() {
		return birthLocation;
	}

	public void setBirthLocation(String birthLocation) {
		this.birthLocation = birthLocation;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public void setBirthDate(GregorianCalendar birthDate) {
		if (birthDate != null) {
			this.birthDate = formatter.format(birthDate.getTime());
		}
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getNobleCondition() {
		return nobleCondition;
	}

	public void setNobleCondition(String nobleCondition) {
		nobleCondition = nobleCondition;
	}

	public String getSpecialStatus() {
		return specialStatus;
	}

	public void setSpecialStatus(String specialStatus) {
		this.specialStatus = specialStatus;
	}

	public String getPhotoHash() {
		return photoHash;
	}

	public void setPhotoHash(String photoHash) {
		this.photoHash = photoHash;
	}

}
