package com.trust1t.easytar.javascriptbridge;

import com.trust1t.android.sdk.eid.core.files.Address;

public class JsAddress {

	private String streetFull;
	private String street;
	private String number;
	private String numberExtra;
	private String zipCode;
	private String municipality;
	
	public JsAddress(Address address)
	{
		this.streetFull = address.getStreetAndNumber();
		this.zipCode = address.getZip();
		this.municipality = address.getMunicipality();
	}
	
	public String getStreetFull() {
		return streetFull;
	}
	public void setStreetFull(String streetFull) {
		this.streetFull = streetFull;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getNumberExtra() {
		return numberExtra;
	}
	public void setNumberExtra(String numberExtra) {
		this.numberExtra = numberExtra;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getMunicipality() {
		return municipality;
	}
	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}
	
	
}
