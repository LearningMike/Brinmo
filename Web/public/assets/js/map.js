//Set up some of our variables.
var map; //Will contain map object.
var maxZoomService; // maximum zoom allowed
var marker = false; // Has the user plotted their location marker? 
var city;
var country;
        
//Function called to initialize / create the map.
//This is called when the page has loaded.
function initMap() {
 
    //The center location of our map.
    var centerOfMap = new google.maps.LatLng(0, 0);
    maxZoomService = new google.maps.MaxZoomService();
 
    //Map options.
    var options = {
        center: centerOfMap, //Set center.
        zoom: 1, //The zoom value. Google if you can set it to maxzoomvalue
        mapTypeId: 'hybrid',
        mapTypeControl: false,
        labels: true
    };
    
    //Create the map object.
    map = new google.maps.Map(document.getElementById('map'), options);

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            var pos = {
                lat: position.coords.latitude,
                lng: position.coords.longitude
            };
            var searchValue = new URLSearchParams(window.location.search);
            var user = searchValue.get("b");
            firebase.database().ref('/searchface/'+user+'/').once('value').then(function(snapshot) {
                //if long and lat exist; hope this works
                if(snapshot.child('lat').val() != null){
                    // old user
                    var oldLong = parseFloat(snapshot.child('long').val());
                    var oldLat = parseFloat(snapshot.child('lat').val());
                    console.log(snapshot.child('lat').val() + " >>> " + oldLat);
                    pos = {
                        lat: oldLat,
                        lng: oldLong
                    };
                    map.setCenter(pos);
                    marker = new google.maps.Marker({
                        position: pos,
                        map: map,
                        draggable: true //make it draggable
                    });
                    document.getElementById('loaderanim').style.display = 'none';
                    maxZoomService.getMaxZoomAtLatLng(pos, function(response) {
                        var maxzoomer = response.zoom;
                        map.setZoom(maxzoomer);
                    });
                } else {
                    map.setCenter(pos);
                    document.getElementById('loaderanim').style.display = 'none';
                    map.setZoom(17);
                }
            });
        });
    } else {
        console.log("Enable location!");
    }
 
    //Listen for any clicks on the map.
    google.maps.event.addListener(map, 'click', function(event) {                
        //Get the location that the user clicked.
        var clickedLocation = event.latLng;
        //make the add button colour reddish when position is edited
        document.getElementById("adbuti").setAttribute("class", "special");
        if(marker === false){
            //Marker hasn't been added, create the marker.
            marker = new google.maps.Marker({
                position: clickedLocation,
                map: map,
                draggable: true //make it draggable
            });
            maxZoomService.getMaxZoomAtLatLng(clickedLocation, function(response) {
                var maxzoomer = response.zoom;
                map.setCenter(clickedLocation);
                map.setZoom(maxzoomer);
            });
            //Listen for drag events!
            google.maps.event.addListener(marker, 'dragend', function(event){
                markerLocation();
            });
        } else{
            //Marker has already been added, so just change its location.
            marker.setPosition(clickedLocation);
            maxZoomService.getMaxZoomAtLatLng(clickedLocation, function(response) {
                var maxzoomer = response.zoom;
                map.setCenter(clickedLocation);
                map.setZoom(maxzoomer);
            });
        }
        //Get the marker's location.
        markerLocation();
    });
}
        
//This function will get the marker's current location and then add the lat/long
//values to our textfields so that we can save the location.
function markerLocation(){
    //Get location.
    var currentLocation = marker.getPosition();
    //Add lat and lng values to a field that we can save.
    document.getElementById('lat').value = currentLocation.lat(); //latitude
    document.getElementById('lng').value = currentLocation.lng(); //longitude
    //Get the city and country name
    var geocoder = new google.maps.Geocoder();
    geocoder.geocode({'latLng': currentLocation}, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
            if (results[1]) {
                //find country and city
                for (var i=0; i<results[0].address_components.length; i++) {
                    for (var b=0;b<results[0].address_components[i].types.length;b++) {
                        if (results[0].address_components[i].types[b] == "locality") {
                            //this is the object you are looking for
                            city = results[0].address_components[i];
                            city = city.long_name;
                            break;
                        }
                        if (results[0].address_components[i].types[b] == "country") {
                            //this is the object you are looking for
                            country = results[0].address_components[i];
                            country = country.long_name;
                            break;
                        }
                    }
                }
            } else {
                alert("No results found");
            }
        } else {
          alert("Geocoder failed due to: " + status);
        }
    });
}