firebase.auth().useDeviceLanguage();
firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL);
firebase.auth().onAuthStateChanged(firebaseUser => {
	if(firebaseUser){
		//user authenticated
		document.getElementById('loaderanim').style.display = 'none';
		var userId = firebase.auth().currentUser.uid;
		var queryValue = new URLSearchParams(window.location.search);
		var queryMonth = null;
		var queryMonthnum = parseInt(queryValue.get('m'));
		if (queryMonthnum >= 0 && queryMonthnum <= 11){
			//query with date from href
			console.log("href based query");
			queryMonth = getMonthname(queryMonthnum);
			var queryYear = queryValue.get('y');
			var queryDate = queryMonthnum + "-" + queryYear;
			document.getElementById('monthyear').innerText = queryMonth + " " + queryYear;
			getTranq(queryDate, userId);
			getLink(queryMonthnum, queryYear, userId);
		} else {
			//query with today's date
			console.log("todays date query");
			var today = new Date();
			var thisMonthnum = today.getMonth();
			var thisYear = today.getFullYear();
			queryMonth = getMonthname(thisMonthnum);
			document.getElementById('monthyear').innerText = queryMonth + " " + thisYear;
			var thisDate = queryMonthnum + "-" + thisYear;
			getTranq(thisDate, userId);
			getLink(thisMonthnum, thisYear, userId);
		}
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

//get month name
function getMonthname(queryMonthnum){
	switch (queryMonthnum) {
		case 0:
			return "january";
		case 1:
			return "february";
		case 2:
			return "march";
		case 3:
			return "april";
		case 4:
			return "may";
		case 5:
			return "june";
		case 6:
			return "july";
		case 7:
			return "august";
		case 8:
			return "september";
		case 9:
			return "october";
		case 10:
			return "november";
		case 11:
			return "december";
	}
}

//get month name
function getLink(queryMonthnum, queryYear, userId){
	var backMonthnum;
	var backMonth;
	var backYear = queryYear;
	var nextMonthnum;
	var nextMonth;
	var nextYear = queryYear;
	switch (queryMonthnum) {
		case 0:
			backMonthnum = 11;
			backMonth = "december";
			backYear = queryYear-1;
			nextMonthnum = 1;
			nextMonth = "february";
			break;
		case 1:
			backMonthnum = 0;
			backmonth = "january";
			nextMonthnum = 2;
			nextMonth = "march";
			break;
		case 2:
			backMonthnum = 1;
			backmonth = "february";
			nextMonthnum = 3;
			nextMonth = "april";
			break;
		case 3:
			backMonthnum = 2;
			backmonth = "march";
			nextMonthnum = 4;
			nextMonth = "may";
			break;
		case 4:
			backMonthnum = 3;
			backMonth = "april";
			nextMonthnum = 5;
			nextMonth = "june";
			break;
		case 5:
			backMonthnum = 4;
			backMonth = "may";
			nextMonthnum = 6;
			nextMonth = "july";
			break;
		case 6:
			backMonthnum = 5;
			backMonth = "june";
			nextMonthnum = 7;
			nextMonth = "august";
			break;
		case 7:
			backMonthnum = 6;
			backMonth = "july";
			nextMonthnum = 8;
			nextMonth = "september";
			break;
		case 8:
			backMonthnum = 7;
			backMonth = "august";
			nextMonthnum = 9;
			nextMonth = "october";
			break;
		case 9:
			backMonthnum = 8;
			backMonth = "september";
			nextMonthnum = 10;
			nextMonth = "november";
			break;
		case 10:
			backMonthnum = 9;
			backMonth = "october";
			nextMonthnum = 11;
			nextMonth = "december";
			break;
		case 11:
			backMonthnum = 10;
			backMonth = "november";
			nextMonthnum = 0;
			nextMonth = "january";
			nextYear = queryYear + 1;
			break;
	}

	var backDate = backMonth + "-" + backYear;
	console.log(backDate);
	//set display to block
	var blinkb = document.getElementById('backlink');
	//set the back link href
	var backpath = "business-transactions?m="+backMonthnum+"&y="+backYear;
	blinkb.href = backpath;

	var nextDate = nextMonth + "-" + nextYear;
	console.log(nextDate);
	//set display to block
	var nlinkb = document.getElementById('nextlink');
	//set the back link href
	var nextpath = "business-transactions?m="+nextMonthnum+"&y="+nextYear;
	nlinkb.href = nextpath;
}

function getTranq(datemonth, userId){
	var searchResult = document.getElementById('searchresults');
	searchResult.innerHTML = "";
	console.log("Before query!!!");
	var tranq = firebase.database().ref('/transactions/'+userId+'/'+datemonth+'/').orderByChild('timestamp');
	console.log("query!!! : "+tranq);
	document.getElementById("transtxt").style.display = "block";
	tranq.on('child_added', function(data) {
		document.getElementById("transtxt").style.display = "none";
		const lili = document.createElement('li');
		var timew = new Date(parseInt(data.val().timestamp));
		var amountinn = "₦" + (data.val().amountinkobo / 100);
		var hours = (timew.getHours()+24)%24; 
		var mid='am';
		if(hours==0){ //At 00 hours we need to show 12 am
			hours=12;	
		} else if(hours>12) {
			hours=hours%12;
			mid='pm';
		}
		var transinfo = data.val().customername + " paid " + amountinn + " | " + timew.getDate() + "/" + (timew.getMonth()+1) + "/" + timew.getFullYear() + " " + hours + ":" + timew.getMinutes() + " " + mid;
		lili.innerText = transinfo;
		//you can take the business to brinmo.com/customer with the data.val().customerid
		//data.val().type is the transaction type: can be transfer/order
		searchResult.insertBefore(lili, searchResult.childNodes[0]);
	});
	console.log("After query!!!");
}