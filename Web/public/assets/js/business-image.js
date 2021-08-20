firebase.auth().useDeviceLanguage();
firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
firebase.auth().onAuthStateChanged(firebaseUser => {
	if(firebaseUser){
        //user authenticated
        var userId = firebase.auth().currentUser.uid;
        document.getElementById('loaderanim').style.display = 'none';

        setUpload("bizimage1", "updimage1", "profileImage1");
        //setUpload("bizimage2", "updimage2", "profileImage2");
        //setUpload("bizimage3", "updimage3", "profileImage3");
        //getImage and display delete button if image exists
        getImage("profileImage1", "updimage1");
        //getImage("profileImage2", "updimage2");
        //getImage("profileImage3", "updimage3");
        
	} else {
		//user not authenticated
		//window.location.pathname = "/login";
	}
});

var newImage = false;

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
function setUpload(bizimage, updimage, profileImage){
    var uploader = document.getElementById('uploader');
    var fileButton = document.getElementById(bizimage);
    fileButton.addEventListener('change', function(e) {
        var userId = firebase.auth().currentUser.uid;
        var storage = firebase.storage();
        var file = e.target.files[0];
        var path = "business/" + userId + "/" + profileImage + ".png";
        var storageRef = storage.ref(path);
        document.getElementById('loaderanim').style.display = 'block';
        uploader.style.opacity = 1;
        var task = storageRef.put(file);
        task.on("state_changed",
            function progress(snapshot){
                var percentage = (snapshot.bytesTransferred/ snapshot.totalBytes) * 100;
                uploader.value = percentage;
            },
            function error(err){
                console.log("upload error: "+err);
            },
            function complete(){
                newImage = true;
                getImage(profileImage, updimage);
                window.location.pathname = "/business-inventory";
            }
        );
    });
}

function getImage(profileImage, updimage){
    var userId = firebase.auth().currentUser.uid;
    var storage = firebase.storage();
    var storageRef2 = storage.ref('business/'+userId+'/'+profileImage+'_600x600.png');

    // Get the download URL
    storageRef2.getDownloadURL().then(function(url) {
        // Insert url into an <img> tag to "download"
        // download the business profile image and display it here
        document.getElementById('uploader').style.opacity = 0;
        document.getElementById('loaderanim').style.display = 'none';
        document.getElementById(updimage).style.backgroundImage = "url('"+url+"')";
        document.getElementById(updimage).style.backgroundRepeat = 'no-repeat';
        document.getElementById(updimage).style.backgroundSize = 'cover';
        document.getElementById(updimage).style.backgroundPosition = '50%';
    }).catch(function(error) {

        // A full list of error codes is available at
        // https://firebase.google.com/docs/storage/web/handle-errors
        switch (error.code) {
            case 'storage/object-not-found':
            // File doesn't exist
            break;

            case 'storage/unauthorized':
            // User doesn't have permission to access the object
            break;

            case 'storage/canceled':
            // User canceled the upload
            break;

            case 'storage/unknown':
            // Unknown error occurred, inspect the server response
            break;
        }
    });
}