function CPCardInserted(cardType) {
	console.log("EASYTAR: Card Inserted of type " + cardType);
	if (cardType == "eid") {
		befa_authenticationSign("test");
	}
}
function CPCardWrongInserted() {
	console.log("EASYTAR: Card Wrong Inserted");
}
function CPCardRemoved() {
	console.log("EASYTAR: Card Removed");
}
function CPReaderConnected() {
	console.log("EASYTAR: Reader Connected");
}
function CPReaderDisconnected() {
	console.log("EASYTAR: Reader Disconnected");
}

function CPEidAuthenticationSignature(signature, error) {
	console.log("EASYTAR: Received authentication signature response");

	if (error == null) {
		console.log(signature);
	}
	else
	{
		console.log(error.message)
	}

}

// Data recievers
function CPEidRnData(rnData, error) {
	console.log("method: CPEIDRnDATA EASYTAR");
	if (error == null) {
		console.log("EASYTAR: received rnData " + rnData);
	} else {
		console.log("ERROR: " + error.message);
	}
}
function CPEidAddressData(addressData, error) {
	if (error == null) {
		console.log("EASYTAR: received address data " + addressData);
	} else {
		console.log("EASYTAR: ERROR: " + error.message);
	}
}

function CPEidAuthenticationCertificate(certificate, error) {
	if (error == null) {
		console.log("EASYTAR: received authentication certificate "
				+ certificate);
	} else {
		console.log("EASYTAR: ERROR: " + error.message);
	}
}

function CPEidRootCertificate(certificate, error) {
	if (error == null || error == "") {
		console.log("EASYTAR: received root certificate " + certificate);
	} else {
		console.log("EASYTAR: ERROR: " + error.message);
	}
}

function CPEidCitizenCertificate(certificate, error) {
	if (error == null || error == "") {
		console.log("EASYTAR: received citizen certificate " + certificate);
	} else {
		console.log("EASYTAR: ERROR: " + error.message);
	}
}
