/** public sdk * */
function befa_getRnData() {
	console.log("asking to get rndata");
	EasyTar.getRnData();
}

function befa_getAddressData() {
	EasyTar.getAddressData();
}

function befa_getRootCertificate() {
	EasyTar.getRootcertificate();
}

function befa_getAuthenticationCertificate() {
	EasyTar.getAuthenticationCertificate();
}

function befa_getCitizenCertificate() {
	EasyTar.getCitizenCertificate();
}

function befa_authenticationSign(toSign) {
	EasyTar.authenticationSign(toSign);
}

function befa_nonrepudiationSign(toSign) {
	EasyTar.nonRepudiationSign(toSign);
}

function _befa_isAndroid() {
	if (typeof EasyTar != "undefined") {
		return true;
	} else {
		return false;
	}
}

/** internal sdk * */

// callbacks from android to this
function _befa_onReaderConnectedCallback() {
	if (readerConnected == false) {
		readerConnected = true;
		CPReaderConnected();
	}
}

function _befa_onEidCardDetected(cardType) {
	console.log("EID card detected, delegating to easytar");
	CPCardInserted(cardType);
}

function _befa_onReaderDisconnectedCallback() {
	console.log("reader disconnected, delegating to easytar");
	readerConnected = false;
	CPReaderDisconnected()
}

// identity delegates

function _CPEidRnData(rnData, error) {
	console.log("rndata...delegate it to easytar...");
	CPEidRnData(rnData, error);
}

function _CPEidAddressData(addressData, error) {
	console.log("addressdata...delegate it to easytar...");
	CPEidAddressData(addressData, error);
}

// certificate delegates
function _CPEidAuthenticationCertificate(certificate, error) {
	console
			.log("authenticationcertificate... received authentication certificate");
	if (certificate)
		CPEidAuthenticationCertificate(certificate.certificate, error);
	else {
		CPEidAuthenticationCertificate(null, error);
	}
}

function _CPEidRootCertificate(certificate, error) {
	console.log("rootcertificate... received root certificate");

	if (certificate)
		CPEidRootCertificate(certificate.certificate, error);
	else {
		CPEidRootCertificate(null, error);
	}
}

function _CPEidCitizenCertificate(certificate, error) {
	console.log("citizen... received root certificate");
	if (certificate)
		CPEidCitizenCertificate(certificate.certificate, error);
	else {
		CPEidCitizenCertificate(null, error);
	}
}

function _CPEidAuthenticationSignature(signature, error) {
	
	if(signature)
		{
		CPEidAuthenticationSignature(signature.signature, error);
		
		}
	else
		{
		CPEidAuthenticationSignature(null, error);
		}
	
}

//signing delegates


// callback functions for android use

function _befa_onReceivedRnDataCallback() {
	console.log("rndata received, let's fetch it");
	var error = jQuery.parseJSON(EasyTar.returnObject('rnDataError'));
	var rnData = jQuery.parseJSON(EasyTar.returnObject('rnData'));
	console.log("fetched rnData");
	_CPEidRnData(rnData, error);
}

function _befa_onReceivedAddressDataCallback() {
	console.log("address received, let's fetch it");
	var error = jQuery.parseJSON(EasyTar.returnObject('addressDataError'));
	var addressData = jQuery.parseJSON(EasyTar.returnObject('addressData'));

	_CPEidAddressData(addressData, error)
}

function _befa_onReceivedRootCertificateCallback() {
	var error = jQuery.parseJSON(EasyTar.returnObject('rootCertificateError'));
	var rootCertificate = jQuery.parseJSON(EasyTar
			.returnObject('rootCertificate'));

	_CPEidRootCertificate(rootCertificate, error);
}

function _befa_onReceivedAuthenticationCertificateCallback() {
	console.log("authenticationcertificate received, let's fetch it");
	var error = jQuery.parseJSON(EasyTar
			.returnObject('authenticationCertificateError'));
	var authenticationCertificate = jQuery.parseJSON(EasyTar
			.returnObject('authenticationCertificate'));

	_CPEidAuthenticationCertificate(authenticationCertificate, error);
}

function _befa_onReceivedCitizenCertificateCallback() {
	console.log("citizen received, let's fetch it");
	var error = jQuery.parseJSON(EasyTar
			.returnObject('citizenCertificateError'));
	var citizenCertificate = jQuery.parseJSON(EasyTar
			.returnObject('citizenCertificate'));

	_CPEidCitizenCertificate(citizenCertificate, error);
}

function _befa_onReceivedAuthenticationSignatureCallback() {

	console.log("received authenticationsignature callback, let's fetch it!");

	var error = jQuery.parseJSON(EasyTar
			.returnObject("authenticationSignatureError"));
	var authenticationSignature = jQuery.parseJSON(EasyTar.returnObject("authenticationSignature"));
	
	_CPEidAuthenticationSignature(authenticationSignature, error);
	
}

var readerConnected = false;
