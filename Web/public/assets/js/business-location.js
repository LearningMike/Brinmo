firebase.auth().useDeviceLanguage();
firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
firebase.auth().onAuthStateChanged(firebaseUser => {
	if(firebaseUser){
        //user authenticated
        //google.maps.event.addDomListener(window, 'load', initMap);
        initMap();
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

//When the form is submitted
document.getElementById('locatinfo').addEventListener('submit', submitForm);
function submitForm(e){
    e.preventDefault();

    var bizlat = document.getElementById('lat').value;
    var bizlng = document.getElementById('lng').value;

    document.getElementById('loaderanim').style.display = 'block';
    if(isNaN(bizlat) || isNaN(bizlng)){
        console.log("Location not selected");
    } else {
        saveStull(bizlat, bizlng);
    }
}

// save tha location
function saveStull(locatlat, locatlong) {
    var searchValue = new URLSearchParams(window.location.search);
    var userId = searchValue.get("b");
    var rUserId = firebase.auth().currentUser.uid;
    
    //get the city and country name with the google places API here
    firebase.database().ref("searchface").child(userId).update({
        "long" : locatlong,
        "lat" : locatlat,
        "city" : city,
        "country" : country
    }).then(function() {
        firebase.database().ref("businessinfo/"+userId+"/cityname").set(city).then(function() {
            firebase.database().ref("businessinfo/"+userId+"/name").once('value').then(function(snap) {
                //save the lowercase name-city
                var bnamed = snap.val();
                bnamed = bnamed.split(" ").join("-");
                var cityd = city.split(" ").join("-");
                var concat = bnamed.toLowerCase() + "-" + cityd.toLowerCase(); 
                firebase.database().ref("businessid/"+concat).set(userId).then(function() {
                    document.getElementById('loaderanim').style.display = 'none';
                    var posterLink = "https://brinmo.com/poster?b="+userId+"&n="+bnamed.split("-").join(" ")+"&c="+cityd;
                    var pageLink = "https://brinmo.com/b/"+concat;
                    var codeLink = "https://brinmo.com/brinmo-code?b="+concat;
                    document.getElementById('poli').href = posterLink;
                    document.getElementById('poli').innerText = posterLink;
                    document.getElementById('pali').href = pageLink;
                    document.getElementById('pali').innerText = pageLink;
                    if (rUserId != userId){
                        document.getElementById('cat').style.display = 'block';
                    }
                }).catch(function(error) {
                    //error
                    console.log("COULD NOT SAVE STUFF "+ error );
                });
            }).catch(function(error) {
                //error
                console.log("COULD NOT SAVE STUFF "+ error );
            });
        }).catch(function(error) {
            //error
            console.log("COULD NOT SAVE STUFF "+ error );
        });
    }).catch(function(error) {
        //error
        console.log("COULD NOT SAVE STUFF "+ error );
    });
}

// save tha category when an option from the popup is clicked
function saveStuct(category) {
    //maybe use this function

    var searchValue = new URLSearchParams(window.location.search);
    var userId = searchValue.get("b");
    document.getElementById('loaderanim').style.display = 'block';
    firebase.database().ref("searchface/"+userId+"/category").set(category).then(function() {
        //close the popup so you can see the links at the bottom
        document.getElementById('cat').style.display = 'none';
        document.getElementById('loaderanim').style.display = 'none'; 
    }).catch(function(error) {
        //error
        console.log("COULD NOT SAVE STUFF "+ error );
    });
}