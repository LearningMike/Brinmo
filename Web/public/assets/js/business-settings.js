firebase.auth().useDeviceLanguage();
firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
firebase.auth().onAuthStateChanged(firebaseUser => {
	if(firebaseUser){
		//user authenticated
		document.getElementById('loaderanim').style.display = 'block';
		var userId = firebase.auth().currentUser.uid;
		firebase.database().ref('/businessinfo/' + userId).once('value').then(function(snapshot) {
			var bizname = snapshot.child('name').val();
			var bizcity = snapshot.child('cityname').val();
			var biznamec = bizname.split(" ").join("-");
			biznamec = biznamec.toLowerCase();
			if (bizcity) {
				var bizcityc = bizcity.split(" ").join("-");
				bizcityc = bizcityc.toLowerCase();
				document.getElementById("xposter").href = "poster?b="+userId+"&n="+bizname+"&c="+bizcity;
				document.getElementById("codex").href = "brinmo-code?b="+biznamec+"-"+bizcityc;
				document.getElementById("bp").href = "/b/"+biznamec+"-"+bizcityc;
				document.getElementById("bl").href = "business-location?b="+userId;
			}
			var bizdelcost = snapshot.child('delcost').val();
			var bizdeltime = snapshot.child('deltime').val();
            firebase.database().ref('/businessbank/' + userId).once('value').then(function(snapsh) {
                var bizbank = snapsh.child('bank').val();
                var biznumber = snapsh.child('anumber').val();
                firebase.database().ref('/businesscontact/' + userId).once('value').then(function(snap) {
                    var phoneNumber = snap.child('pnumber').val();
	                document.getElementById('demo-number').value = biznumber;
                    if(phoneNumber[3] == 4){
                        phoneNumber = phoneNumber.slice(4,phoneNumber.length);
                        phoneNumber = "0"+phoneNumber;
					}
					var bizInd = getInd(bizbank);
					document.getElementById('demo-pnumber').value = phoneNumber;
					document.getElementById('demo-cost').value = bizdelcost;
					document.getElementById('demo-time').selectedIndex = bizdeltime[0];
					document.getElementById('demo-bank').selectedIndex = bizInd;
                    document.getElementById('loaderanim').style.display = 'none';
                });
            });
        });
	} else {
		//user not authenticated
		document.getElementById('loaderanim').style.display = 'block';
		window.location.pathname = "/add-your-business";
	}
});

//get index of the value
function getInd(bizbank){
    var bankindex = null;
	switch (bizbank) {
		case "Fidelity Bank":
			bankindex = 1;
			break;
		case "Guaranty Trust Bank":
			bankindex = 2;
            break;
        case "Zenith Bank":
			bankindex = 3;
            break;
        case "United Bank For Africa":
			bankindex = 4;
            break;
        case "First Bank of Nigeria":
			bankindex = 5;
            break;
        case "First City Monument Bank":
			bankindex = 6;
            break;
        case "Union Bank of Nigeria":
			bankindex = 7;
            break;
        case "Access Bank":
			bankindex = 8;
            break;
        case "Sterling Bank":
			bankindex = 9;
            break;
        case "Heritage Bank":
			bankindex = 10;
            break;
        case "Wema Bank":
			bankindex = 11;
            break;
        case "Unity Bank":
			bankindex = 12;
            break;
        case "Standard Chartered Bank":
			bankindex = 13;
            break;
        case "Stanbic IBTC Bank":
			bankindex = 14;
            break;
        case "Skye Bank":
			bankindex = 15;
            break;
        case "Mainstreet Bank":
			bankindex = 16;
            break;
        case "Keystone Bank":
			bankindex = 17;
            break;
        case "Enterprise Bank":
			bankindex = 18;
            break;
        case "Ecobank Nigeria":
			bankindex = 19;
            break;
        case "Citibank Nigeria":
			bankindex = 20;
			break;
		case "Kuda Bank":
			bankindex = 21;
            break;
        case "Jaiz Bank":
			bankindex = 22;
            break;
        case "Taj Bank":
			bankindex = 23;
			break;
	}
	return bankindex;
}

//When form has been edited
function makereddish(formid) {
    document.getElementById(formid).setAttribute("class", "special");
} 

//When the form is submitted
document.getElementById('bizinfo').addEventListener('submit', submitForm);
function submitForm(e){
	e.preventDefault();
	document.getElementById('loaderanim').style.display = 'block';
	console.log("submitted!");
	var bizdelcost = document.getElementById('demo-cost').value;
	var bizdeltime = document.getElementById('demo-time').value;
	saveStuff(bizdelcost, bizdeltime);
}

//When the form is submitted
document.getElementById('bankinfo').addEventListener('submit', submitBorm);
function submitBorm(e){
	e.preventDefault();
	document.getElementById('loaderanim').style.display = 'block';
	console.log("submitted!");
	var bizbank = document.getElementById('demo-bank').value;
	var biznumber = document.getElementById('demo-number').value;
	saveStubb(bizbank, biznumber);
}

function saveStubb(bank, anumber){
	//delete old bank first to set a new bank
	const userId = firebase.auth().currentUser.uid;
	firebase.database().ref("businessbank").child(userId).remove().then(function(){
		firebase.database().ref("businessbank").child(userId).set({
			"bank" : bank,
			"anumber" : anumber
		}).then(function() {
			document.getElementById("bankinfob").setAttribute("class", "default");
			document.getElementById('loaderanim').style.display = 'none';
		}).catch(function(error) {
			//error
			console.log("COULD NOT SAVE BANK "+ error );
		});
	}).catch(function(error) {
		//error
		console.log("COULD DELETE LAST BANK "+ error );
	});
}

function saveStuff(delcost, deltime) {
	//adding business info
	const userId = firebase.auth().currentUser.uid;
	firebase.database().ref("businessinfo").child(userId).update({
		"description" : 3,
		"delcost" : delcost,
		"deltime" : deltime
	}).then(function() {
		document.getElementById("bizinfob").setAttribute("class", "default");
		document.getElementById('loaderanim').style.display = 'none';
	}).catch(function(error) {
		//error
		console.log("COULD NOT SAVE STUFF "+ error );
	});
}

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
	phoneNumber = "";
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
		document.getElementById("firstSubmit").setAttribute("class", "default");
		document.getElementById('loaderanim').style.display = 'none';
	}).catch(function(error) {
		//error
		console.log("COULD NOT SAVE STUFF "+ error );
	});
}

// hoveron
function hoveron(tab){
	document.getElementById("hoveron").innerText = tab;
	
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