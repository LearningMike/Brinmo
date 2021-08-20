firebase.auth().useDeviceLanguage();
firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
firebase.auth().onAuthStateChanged(firebaseUser => {
	if(firebaseUser){
		//user authenticated
		var userId = firebase.auth().currentUser.uid;
		firebase.database().ref('/businesscontact/' + userId + '/pnumber').once('value').then(function(snapshot) {
			var reg = snapshot.val() || "na";
			console.log(reg);
  			if (reg != "na"){
				//user is registered
				firebase.database().ref('/businessinfo/' + userId).once('value').then(function(snapshot) {
					var bcity = snapshot.val().cityname || "na"
					console.log(bcity);
					if (bcity != "na"){
					  	//business has location
						document.getElementById('hasloc').style.display = 'block';
						document.getElementById('noloc').style.display = 'none';
						document.getElementById('loaderanim').style.display = 'none';
						//show business info
						var bname = snapshot.val().name;
						//set the business link
						var blinkname = bname.split(' ').join('-');
						var blinkcity = bcity.split(' ').join('-');
						blinkname = blinkname.toLowerCase();
						blinkcity = blinkcity.toLowerCase();
						document.getElementById('bizlink').innerText = 'brinmo.com/b/'+blinkname+'-'+blinkcity;
						document.getElementById('bizlink').href = 'https://brinmo.com/b/'+blinkname+'-'+blinkcity;
						document.getElementById('bizlink').style.display = 'block';
						var bzatz = snapshot.val().satisfied;
						var bnocus = snapshot.val().nofcustomers;
						document.getElementById('bizzyname').innerText = bname+" "+bcity;
						document.getElementById('customerz').innerText = bnocus;
						document.getElementById('zatisfied').innerText = bzatz;
						var customerList = document.getElementById('searchresults');
						customerList.innerHTML = "";
						var cusq = firebase.database().ref('/bizcustomers/'+userId+'/'); 
						cusq.on('child_added', function(data) {
							document.getElementById("custxt").style.display = "none";
							const lili = document.createElement('li');
							lili.setAttribute("id", "cusbar");
							var cusinfo = data.val().customername;
							var satisfied = data.val().satisfied;
							if (satisfied = "1" || satisfied == "2") {
								lili.innerText = cusinfo + " 😍";
							} else {
								lili.innerText = cusinfo + " 😡";
							}
							//searchResult.appendChild(lili);
							customerList.insertBefore(lili, customerList.childNodes[0]);
						});
					} else {
						//business is new
						document.getElementById('hasloc').style.display = 'none';
						document.getElementById('noloc').style.display = 'block';
						document.getElementById('loaderanim').style.display = 'none';
					}
				});
			} else {
				window.location.pathname = "/business-registration";
			}
		});
		//verify email popup?
		const emailVerif = firebase.auth().currentUser.emailVerified;
	} else {
		//user not authenticated
		window.location.pathname = "/login";
	}
});

// hoveron
function hoveron(tab){
	document.getElementById("hoveron").innerText = tab;
	
}

// logout
function logOuter(){
	document.getElementById('loaderanim').style.display = 'block';
    firebase.auth().signOut().then(function() {
          // Sign-out successful.
        window.location.href = "https://brinmo.com";
    }).catch(function(error) {
          // An error happened.
    });
}

/* Register service worker.
if ('serviceWorker' in navigator) {
	window.addEventListener('load', function() {
	  navigator.serviceWorker.register('service-worker.js')
		.then(reg => {
		  console.log('Service worker registered! 😎', reg);
		})
		.catch(err => {
		  console.log('😥 Service worker registration failed: ', err);
		});
	});
}

let deferredPrompt;
window.addEventListener('beforeinstallprompt', event => {

    // Prevent Chrome 67 and earlier from automatically showing the prompt
    event.preventDefault();

	// Stash the event so it can be triggered later.
	deferredPrompt = event;

	// Attach the install prompt to a user gesture
	document.querySelector('#installBtn').addEventListener('click', event => {

		// Show the prompt
		deferredPrompt.prompt();

		// Wait for the user to respond to the prompt
		deferredPrompt.userChoice.then((choiceResult) => {
			if (choiceResult.outcome === 'accepted') {
				console.log('User accepted the A2HS prompt');
			} else {
				console.log('User dismissed the A2HS prompt');
			}
			deferredPrompt = null;
		});
	});

	// Update UI notify the user they can add to home screen
	document.querySelector('#installBanner').style.display = 'flex';
});*/