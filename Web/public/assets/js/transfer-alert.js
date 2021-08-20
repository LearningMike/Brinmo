firebase.auth().useDeviceLanguage();
firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
firebase.auth().onAuthStateChanged(firebaseUser => {
	if(firebaseUser){
		//user authenticated
		document.getElementById('loaderanim').style.display = 'block';
		var userId = firebase.auth().currentUser.uid;
		firebase.database().ref('/businesscontact/' + userId + '/pnumber').once('value').then(function(snapshot) {
  			var reg = snapshot.val() || "na";
  			if (reg != "na"){
				//user has already registered
				window.location.pathname = "/dashboard";
			} else {
				document.getElementById('loaderanim').style.display = 'none';
			}
		});
 	} else {
		//user not authenticated
		document.getElementById('loaderanim').style.display = 'block';
		window.location.pathname = "/add-your-business";
	}
});

//When the phone number form is submitted
document.getElementById('verifyNumber').addEventListener('submit', submitPorm);
function submitPorm(e){
	e.preventDefault();

	document.getElementById('loaderanim').style.display = 'block';
	var country = document.getElementById('demo-country').value;
	var rnumber = document.getElementById('demo-pnumber').value;

	// Phone Number Transformation Start
	var rphoneNumber = new Array();
	for(i=0; i<rnumber.length; i++){
		j=-1;
		if(!isNaN(rnumber[i]) && rnumber[i]!=" "){
			j= j+1;
			rphoneNumber = rphoneNumber.concat(rnumber[i]); 
		}
	}
	if(rphoneNumber[0] == 0){
		rphoneNumber = rphoneNumber.slice(1,rphoneNumber.length);
	}
	var phoneNumber = "";
	for(i=0; i<rphoneNumber.length; i++){
		phoneNumber = phoneNumber+rphoneNumber[i];
	}
	phoneNumber = country+phoneNumber;
	// Phone Number Transformation End

	// save stuff
	saveStuee(phoneNumber);
}

function saveStuee(phoneNumber){
	const userId = firebase.auth().currentUser.uid;
	const userEmail = firebase.auth().currentUser.email;
	firebase.database().ref("businesscontact").child(userId).update({
		"email" : userEmail,
		"pnumber" : phoneNumber
	}).then(function() {
		firebase.auth().currentUser.sendEmailVerification().then(function() {
			// Email sent.
			window.location.pathname = "/business-image";
		}).catch(function(error) {
  			// An error happened.
		});
	}).catch(function(error) {
		//error
		console.log("COULD NOT SAVE STUFF "+ error );
	});
}

// logout
function logOuter(){
	firebase.auth().signOut().then(function() {
		// Sign-out successful.
		window.location.href = "https://brinmo.com";
	}).catch(function(error) {
		// An error happened.
	});
}