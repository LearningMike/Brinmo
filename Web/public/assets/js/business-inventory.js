firebase.auth().useDeviceLanguage();
firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
firebase.auth().onAuthStateChanged(firebaseUser => {
	if(firebaseUser){
		//user authenticated
		var userId = firebase.auth().currentUser.uid;
		firebase.database().ref('businessinfo/' + userId + '/name').once('value').then(function(snapshot) {
			bname = snapshot.val();
		});
		// check if delcost > 0
		firebase.database().ref('businessinfo/' + userId + '/delcost').once('value').then(function(snapshot) {
			var dels = snapshot.val() || 0;
			if (dels != "0") {
				//business delivers
				document.getElementById('delivs').style.display = 'block';
				document.getElementById('noitem').style.display = 'none';
				document.getElementById('loaderanim').style.display = 'none';

				var city;
		
				firebase.database().ref('searchface/' + userId).once('value').then(function(snapshot) {
					city = snapshot.child('city').val();
					if (city == null || city == "" || city.length < 2){
						//alert("Your business needs to be verified first!");
						//window.location.pathname = "/business-location";
					}
					longitude = snapshot.child('long').val();
					latitude = snapshot.child('lat').val();
				});
				
				//get inventory
				var inventoryList = document.getElementById('searchresults');
				inventoryList.innerHTML = "";
				var invq = firebase.database().ref('/bizinventory/'+userId+'/'); 
				invq.on('child_added', function(data) {
					document.getElementById("invtxt").style.display = "none";
					var itemid = data.key;
					itemid = itemid.replaceAll("'","\'");
					itemid = itemid.toLowerCase();
					var cost = data.val().cost;
					cost = parseInt(cost);
					var pulse = data.val().pulse;
					var itemname = itemid.split("-").join(" ");
					
					var divya = document.createElement('div');
					divya.setAttribute("style", "display: inline-block; margin: 1vw; border-radius: 10px; width: 180px; background-color: #fff; box-shadow: #00001111 0px 0px 20px 5px;");
					divya.innerHTML = 	'<div id="'+itemid+'" class="itembox" style="height: 120px; width: 180px; border-bottom-left-radius: 0px; border-bottom-right-radius: 0px;">'+
											'<input type="checkbox" id="p-'+itemid+'">'+
											'<label id="l-'+itemid+'" onclick="pulse('+pulse+',`'+itemid+'`,`'+itemname+'`,`'+city+'`,`'+bname+'`,'+cost+')" for="p-'+itemid+'" title="ready?" style="float: right; margin-top: 15px; margin-right: 10px;"></label>'+
										'</div>'+
										'<div style="font-size: 16px; padding-left: 10px; max-width: 180px; text-transform: capitalize;">'+itemname+'</div>'+
										'<span style="font-size: 14px; padding-left: 20px; float: left; line-height: normal;">₦'+cost+'</span>'+
										'<button onclick="showItem(`'+itemid+'`, '+cost+')" class="default fa fa-edit" style="opacity: 0.6; padding-left: 10px; padding-right: 10px; font-weight: lighter; text-transform: lowercase; float: right; height: 32px; line-height: unset; border-bottom-right-radius: 10px; border-top-left-radius: 10px;"></button>';
					inventoryList.appendChild(divya, inventoryList.childNodes[0]);
					showPulse(pulse, itemid);
					getImage(itemid, itemid);
				});
			} else {
				//business doesn't deliver
				document.getElementById('delivs').style.display = 'none';
				document.getElementById('noitem').style.display = 'flex';
				document.getElementById('loaderanim').style.display = 'none';
			}
		});
	} else {
		//user not authenticated
		window.location.pathname = "/login";
	}
});

//sets delivery time and cost
function setDelivery(){
	var userId = firebase.auth().currentUser.uid;
	const costofdel = document.getElementById("demo-cost").value;
	const timefordel = document.getElementById("demo-time").value;
	firebase.database().ref("businessinfo").child(userId).update({
		"delcost" : costofdel,
		"deltime" : timefordel
	}).then(function() {
		//switch
		document.getElementById('delivs').style.display = 'block';
		document.getElementById('noitem').style.display = 'none';
		document.getElementById('loaderanim').style.display = 'none';

		var city;
		
		firebase.database().ref('searchface/' + userId + '/city').once('value').then(function(snapshot) {
			city = snapshot.val();
			if (city == null || city == "" || city.length < 2){
				//alert("Your business needs to be verified first!");
				//window.location.pathname = "/business-location";
			}
		});
				
		//get inventory
		var inventoryList = document.getElementById('searchresults');
		inventoryList.innerHTML = "";
		var invq = firebase.database().ref('/bizinventory/'+userId+'/'); 
		invq.on('child_added', function(data) {
			document.getElementById("invtxt").style.display = "none";
			var itemid = data.key;
			itemid = itemid.replaceAll("'","\'");
			itemid = itemid.toLowerCase();
			var cost = data.val().cost;
			cost = parseInt(cost);
			var pulse = data.val().pulse;
			var itemname = itemid.split("-").join(" ");
			
			var divya = document.createElement('div');
			divya.setAttribute("style", "display: inline-block; margin: 1vw; border-radius: 10px; width: 180px; background-color: #fff; box-shadow: #00001111 0px 0px 20px 5px;");
			divya.innerHTML = 	'<div id="'+itemid+'" class="itembox" style="height: 120px; width: 180px; border-bottom-left-radius: 0px; border-bottom-right-radius: 0px;">'+
									'<input type="checkbox" id="p-'+itemid+'">'+
									'<label id="l-'+itemid+'" onclick="pulse('+pulse+',`'+itemid+'`,`'+itemname+'`,`'+city+'`,`'+bname+'`,'+cost+')" for="p-'+itemid+'" title="ready?" style="float: right; margin-top: 15px; margin-right: 10px;"></label>'+
								'</div>'+
								'<div style="font-size: 16px; padding-left: 10px; max-width: 180px; text-transform: capitalize;">'+itemname+'</div>'+
								'<span style="font-size: 14px; padding-left: 20px; float: left; line-height: normal;">₦'+cost+'</span>'+
								'<button onclick="showItem(`'+itemid+'`, '+cost+')" class="default fa fa-edit" style="opacity: 0.6; padding-left: 10px; padding-right: 10px; font-weight: lighter; text-transform: lowercase; float: right; height: 32px; line-height: unset; border-bottom-right-radius: 10px; border-top-left-radius: 10px;"></button>';
			inventoryList.appendChild(divya, inventoryList.childNodes[0]);
			showPulse(pulse, itemid);
			getImage(itemid, itemid);
		});

		// upload first item
		showItem("+");
	}).catch(function(error) {
		// error
		console.log("COULD NOT SAVE STUFF "+ error );
	});
};

//opens a popup for a new or existing item
function showItem(id, costot){
	location.hash = id;
	console.log("id-price: "+id+"_"+costot);
	document.getElementById('addder').style.display = "none";
	document.getElementById("itempopup").style.display = "block";
	if (id == "+"){
		document.getElementById('itemsave').setAttribute("onclick", "saveItem('+')");
		document.getElementById('delItem').setAttribute("onclick", "deleteItem('+')");
		document.getElementById('cloItem').setAttribute("onclick", "closeItem('+')");
		setUpload("bizimage1");
	} else {
		document.getElementById('itemsave').setAttribute('onclick', 'saveItem("'+id+'")');
		document.getElementById('delItem').setAttribute('onclick', 'deleteItem("'+id+'")');
		document.getElementById('cloItem').setAttribute('onclick', 'closeItem("'+id+'")');
		//set the content from the id
		document.getElementById('demo-item').value = id.split("-").join(" ");
		document.getElementById('demo-price').value = costot;
		getImage(id, "updimage1");
		setUpload("bizimage1");
	}
};

function closeItem(id){
	location.hash = "";
	document.getElementById('addder').style.display = "block";
	document.getElementById("demo-item").value = "";
	document.getElementById("demo-price").value = "";
	document.getElementById("itempopup").style.display = "none";
	document.getElementById('loaderanim').style.display = 'none';
}
var pathrefer = "";
var iname = "";
var iprice = "";
var newId = "";
var bname = "";
var longitude = "";
var latitude = "";

function saveItem(id){
	var userId = firebase.auth().currentUser.uid;
	//if id is + save new else save the id item
	if (id == "+"){
		//push new id
		iname = document.getElementById("demo-item").value;
		iprice = document.getElementById("demo-price").value;
		iname = iname.trim();
		newId = iname.split(" ").join("-");
		newId = newId.toLowerCase();
		document.getElementById('loaderanim').style.display = 'block';
		//uploader.style.opacity = 1;
		pathrefer = "business/" + userId + "/" + newId + ".png";
		var file = document.getElementById('bizimage1').files[0];
		/**if (file) {
			var url = reader.readAsDataURL(file);
		}*/
		var storage = firebase.storage();
		var storageRef = storage.ref(pathrefer);
		var task = storageRef.put(file);
		task.on("state_changed", function progress(snapshot){
			var percentage = (snapshot.bytesTransferred/snapshot.totalBytes) * 100;
			//uploader.value = percentage;
		}, function error(err){
			console.log("upload error: "+err);
		}, function complete(){
			firebase.database().ref("bizinventory/"+userId+"/"+newId).set({
				"name" : iname,
				"cost" : iprice,
				"pulse": 2
			}).then(function() {
				//uploader.style.opacity = 0;
				closeItem("");
			}).catch(function(error) {
				//error
				console.log("COULD NOT SAVE NEW ITEM "+ error );
			});
		});
	} else {
		//use old  id
		//note: you can't change the name of an item, only delete
		var iprice = document.getElementById("demo-price").value;
		firebase.database().ref("bizinventory/"+userId+"/"+id).update({
			"cost" : iprice,
			"pulse" : 2
		}).then(function() {
			//edit the cost and pulse on screen
			closeItem("");
		}).catch(function(error) {
			//error
			console.log("COULD NOT UPDATE ITEM "+ error );
		});
	}
};

function deleteItem(id){
	document.getElementById('loaderanim').style.display = 'block';
	var userId = firebase.auth().currentUser.uid;
	if (id == "+"){
		closeItem("+");
	} else {
		firebase.database().ref("bizinventory/"+userId+"/"+id).remove().then(function() {
			//delete image
			firebase.storage().ref("business/"+userId+"/"+id+"_600x600.png").delete();
			closeItem("");
			//refresh page
			location.reload();
		}).catch(function(error) {
			//error
			console.log("COULD NOT DELETE ITEM "+ error );
		});
	}
};

//pulse
function pulse(pulse, itemid, itemname, city, bname, cost){
	//set item pulse if city is not null

	if (city != null){

		var userId = firebase.auth().currentUser.uid;

		if (pulse == 1){
			//livefeed message 
			city = city.toLowerCase();
			document.getElementById("pmessage").innerText = '"'+itemname+'" not available';
			firebase.database().ref("bizinventory/"+userId+"/"+itemid+"/pulse").set(0).then(function() {
				document.getElementById("p-"+itemid).checked = false;
				//delete from local livefeed
				firebase.database().ref("livefeed/"+city+"-nigeria/"+itemid).remove().then(function() {
					//reset the pulse onclick function with attr
					document.getElementById('l-'+itemid).setAttribute("onclick", "pulse("+0+",`"+itemid+"`,`"+itemname+"`,`"+city+"`,`"+bname+"`,"+cost+")");
				});
			}).catch(function(error) {
				//error
				console.log("COULD NOT UPDATE ITEM PULSE" + error );
			});
		} else if (pulse == 0 || pulse == 2){
			//livefeed message
			city = city.toLowerCase();
			document.getElementById("pmessage").innerText = '"'+itemname+'" 🔥';
			firebase.database().ref("bizinventory/"+userId+"/"+itemid+"/pulse").set(1).then(function() {
				document.getElementById("p-"+itemid).checked = true;
				//save to local livefeed
				firebase.database().ref("livefeed/"+city+"-nigeria/"+itemid).update({
					"x" : userId,
					"n" : bname,
					"c" : cost,
					"t" : Date.now()
				}).then(function() {
					//reset the pulse onclick function with attr
					document.getElementById('l-'+itemid).setAttribute("onclick", "pulse("+1+",`"+itemid+"`,`"+itemname+"`,`"+city+"`,`"+bname+"`," +cost+")");
				});
			}).catch(function(error) {
				//error
				console.log("COULD NOT UPDATE ITEM PULSE" + error );
			});
		}
	} else {
		document.getElementById("pmessage").innerText = 'cannot post to livefeed until business is verified';
	}
}

function showPulse(pulse, itemid){
	if (pulse == 1){
		document.getElementById("p-"+itemid).checked = true;
	} else if (pulse == 0 || pulse == 2){
		document.getElementById("p-"+itemid).checked = false;
	}
}

// hoveron
function hoveron(tab){
	document.getElementById("hoveron").innerText = tab;
	
}

function setUpload(bizimage){
	//bizimage is the uploaded file
	//var uploader = document.getElementById('uploader');
	document.getElementById("updimage1").style.backgroundImage = "";
    var fileButton = document.getElementById(bizimage);
    fileButton.addEventListener('change', function(e) {
		document.getElementById('loaderanim').style.display = 'block';
        var file = e.target.files[0];
		if (file) {
			var reader = new FileReader();
			reader.onload = function(){
				var url = reader.result;
				console.log(url);
				document.getElementById("updimage1").style.backgroundImage = 'url("'+url+'")';
				document.getElementById("updimage1").style.backgroundRepeat = 'no-repeat';
				document.getElementById("updimage1").style.backgroundSize = 'cover';
				document.getElementById("updimage1").style.backgroundPosition = '50%';
				document.getElementById('loaderanim').style.display = 'none';
			};
			reader.readAsDataURL(file);
			
		} else {
			document.getElementById('loaderanim').style.display = 'none';
		}
        
    });
}

function getImage(profileImage, updimage){
	//profileImage should be the item id
	//updimage should be the image display id (can be the same as item id)
    var userId = firebase.auth().currentUser.uid;
    var storage = firebase.storage();
    var storageRef2 = storage.ref('business/'+userId+'/'+profileImage+'_600x600.png');

    // Get the download URL
    storageRef2.getDownloadURL().then(function(urler) {
        // Insert url into an <img> tag to "download"
        // download the business profile image and display it here
        document.getElementById(updimage).style.backgroundImage = 'url("'+urler+'")';
        document.getElementById(updimage).style.backgroundRepeat = 'no-repeat';
        document.getElementById(updimage).style.backgroundSize = 'cover';
        document.getElementById(updimage).style.backgroundPosition = '50%';
    }).catch(function(error) {

        // full list of error codes are available at
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