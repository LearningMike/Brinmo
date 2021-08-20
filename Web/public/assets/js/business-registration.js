firebase.auth().useDeviceLanguage();
firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
firebase.auth().onAuthStateChanged(firebaseUser => {
	if(firebaseUser){
		//user authenticated
		document.getElementById('loaderanim').style.display = 'block';
		var userId = firebase.auth().currentUser.uid;
		firebase.database().ref('/businesscontact/' + userId + '/pnumber').once('value').then(function(snapshot) {
  			var reg = snapshot.val();
  			if (reg != null && reg.length > 7){
				//user has already registered
				window.location.pathname = "/dashboard";
			}
		});
		document.getElementById('loaderanim').style.display = 'none';
	} else {
		//user not authenticated
		document.getElementById('loaderanim').style.display = 'block';
		window.location.pathname = "/add-your-business";
	}
});

//When the form is submitted
document.getElementById('bizinfo').addEventListener('submit', submitForm);
function submitForm(e){
	e.preventDefault();
	document.getElementById('loaderanim').style.display = 'block';
	console.log("submitted!");
	var bizname = document.getElementById('demo-name').value;
	var biztitle = document.getElementById('demo-title').value;
	var bizbank = document.getElementById('demo-bank').value;
	var biznumber = document.getElementById('demo-number').value;
	bizname = bizname.trim();
	biztitle = biztitle.trim();
	saveStuff(bizname, biztitle, bizbank, biznumber);
}

function saveStubb(userId, bank, anumber){
	firebase.database().ref("businessbank").child(userId).set({
		"bank" : bank,
		"anumber" : anumber
	}).then(function() {
		window.location.pathname = "/transfer-alert";
	}).catch(function(error) {
		//error
		console.log("COULD NOT SAVE STUFF "+ error );
	});
}

function saveStuff(name, title, bank, anumber) {
	//adding business info
	const userId = firebase.auth().currentUser.uid;
	firebase.database().ref("businessinfo").child(userId).set({
		"name" : name,
		"title" : title,
		"description" : 0,
		"satisfied" : 0,
		"nofcustomers" : 0,
		"delcost": "0",
		"deltime": "0"
	}).then(function() {
		saveStubb(userId, bank, anumber);
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