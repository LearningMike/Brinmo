// get charge
var bcharge = document.getElementById("bcharge");
var ccharge = document.getElementById("ccharge");
var mval = document.getElementById("mval");
var amnt = document.getElementById("amnt");
function getCharge(){
	amnt.innerText = "₦"+mval.value;
	bcharge.innerText = "₦" + ((mval.value / 100) * 4);
}