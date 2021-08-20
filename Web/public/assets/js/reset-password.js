firebase.auth().useDeviceLanguage();
firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
firebase.auth().onAuthStateChanged(firebaseUser => {
	if(firebaseUser){
		//user authenticated
		var email = firebase.auth().currentUser.email;
		document.getElementById('demo-email').value = email;
		document.getElementById('loaderanim').style.display = 'none';
	} else {
		//user not authenticated
		console.log("not logged in");
		document.getElementById('loaderanim').style.display = 'none';
	}
});

//When the reset form is submitted
document.getElementById('reset').addEventListener('submit', submitSorm);
function submitSorm(e){
	e.preventDefault();

	document.getElementById('loaderanim').style.display = 'block';
	var resetemail = document.getElementById('demo-email').value;
	//Send reset Password Email
	firebase.auth().sendPasswordResetEmail(resetemail).then(function() {
		document.getElementById('loaderanim').style.display = 'none';
		alert("Check your email: "+resetemail+", We sent a password reset link !");
		window.location.pathname = "/login";
	}).catch(function(error) {
		// An error happened.
		console.log(error);
	});
}