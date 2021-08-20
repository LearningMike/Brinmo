// Load Environment variables
//require('dotenv').load();

// paystack module is required to make charge token call
const paystack = require('paystack')('sk_live_fe15d79d28e8ea1b009253c0c0a90b27c639b92b');
const GeoFire = require('geofire');
const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

const cors = require('cors');
const express =  require('express');

const appy = express();
const appier = express();
const engines = require('consolidate');
const https = require('https');

appy.engine('hbs', engines.handlebars);
appy.set('views', './views');
appy.set('view engine', 'hbs');

appier.engine('hbs', engines.handlebars);
appier.set('views', './views');
appier.set('view engine', 'hbs');

const app = express();
const bodyParser = require('body-parser');

app.set('port', (3002));
app.use(express.static(__dirname + '/public'));
app.use(bodyParser.json());       // to support JSON-encoded bodies
app.use(bodyParser.urlencoded({   // to support URL-encoded bodies
    extended: true
}));

/** useful codes

    ////sendmail
    const mailOptions = {
        from: 'bizzy notifier <michael.abia@aun.edu.ng>', // Something like: Jane Doe <janedoe@gmail.com>
        to: 'bizzybusinesspeople@gmail.com',
        subject: 'bizzy mail test', // email subject
        html: `<p style="font-size: 16px;">Payment Alert test</p>
            <br />
            <p>Customer paid business "Reference" amount</p>
        ` // email content in HTML
    };
    transporter.sendMail(mailOptions);

    ////create subaccount?
    paystack.subaccount.create({
        settlement_bank: snapshot.val().bank,
        account_number: snapshot.val().anumber,
        percentage_charge: 3.9
    });
*/
/*
    Laterrr...
    Create subaccount based on remote config country psp (psp-ng:paystack) [Create when bankinfo is edited]
    Automatic Messaging: adding reviews, questions as messages
    Preventing Spam, Database Abuse or Scraping.
 */

//////////////////////////////////////////////////////////////////////////////////////////////////////////

exports.makeSignup = functions.database.ref('/businessbank/{bizId}')
    .onCreate((change, context) => {
        // get a subaccount code for the business
        var bizbank = change.val().bank;
        var accnumber = change.val().anumber;
        var bizid = context.params.bizId;

        return admin.database().ref('/businessinfo/'+bizid+'/name').once('value').then(snap => {
            //get the business name
            console.log("2:");
            const params = JSON.stringify({
                "business_name": "A:"+bizid+":"+bizbank,
                "settlement_bank": bizbank, 
                "account_number": accnumber, 
                "percentage_charge": 4,
                "description": "a trusted and verified small business"
            });

            const options = {
                hostname: 'api.paystack.co',
                port: 443,
                path: '/subaccount',
                method: 'POST',
                headers: {
                    Authorization: 'Bearer sk_live_fe15d79d28e8ea1b009253c0c0a90b27c639b92b',
                    'Content-Type': 'application/json'
                }
            }

            const req = https.request(options, res => {
                let datas = '';
                res.on('data', (chunk) => {
                    datas += chunk;
                });
                res.on('end', () => {
                    var datax = JSON.parse(datas);
                    admin.database().ref('/businesscontact/'+bizid).update({
                        'code': datax.data.subaccount_code,
                        'psp': 'paystack'
                    }).catch(reason => {
                        console.log("Adding subaccode("+datax.data.subaccount_code+") to RTDB failed: "+reason);
                    });
                    console.log("1: ");
                });
            }).on('error', error => {
                console.error(error);
            });

            req.write(params);
            req.end();
        }).catch(reason => {
            //error
            console.log("COULD NOT CREATE SUBACCOUNT: "+ reason);
        });
    });

//////////////////////////////////////////////////////////////////////////////////////////////////////////

    exports.makeChacha = functions.database.ref('/usercharge/{uid}')
    .onCreate((change, context) => {
        // This is the server (main) function to charge money
        // it only works on people who have already been charged on the app and saved chargeable cards
        // test this later, hope it works
        var email = change.val().email;
        var amount = change.val().amount;
        var auth = change.val().auth;
        var subcode = change.val().subcode;
        var uid = context.params.uid;
        var timeinmilli = new Date();

        const params = JSON.stringify({
            "authorization_code" : auth,
            "email" : email,
            "amount" : amount,
            "subaccount_code" : subcode,
            "currency": "NGN",
            "reference": uid+"_T_"+timeinmilli
        });

        const options = {
            hostname: 'api.paystack.co',
            port: 443,
            path: '/transaction/charge_authorization',
            method: 'POST',
            headers: {
                Authorization: 'Bearer sk_live_fe15d79d28e8ea1b009253c0c0a90b27c639b92b',
                'Content-Type': 'application/json'
            }
        }

        const req = https.request(options, res => {
            let datas = '';
            res.on('data', (chunk) => {
                datas += chunk;
            });
            res.on('end', () => {
                console.log("1: " + JSON.parse(datas));
                admin.database().ref('/usercharge/'+uid).remove();
            });
        }).on('error', error => {
            console.error(error);
        });

        req.write(params);
        req.end();
    });

//////////////////////////////////////////////////////////////////////////////////////////////////////////

exports.makeConversion = functions.database.ref('/cusbusinesses/{cusId}/satisfied/{bizId}')
    .onCreate((change, context) => {
        // Add the user to satisfied and s
        var bizid = context.params.bizId;
        var uid = context.params.cusId;

        //user is already a bizcustomer

        return admin.database().ref('/businessinfo/'+bizid+'/satisfied').transaction(post => {
            console.log("Business satisfied Increased: "+ post);
            return (post || 0)+1;
        }).then(snap => {
            //get the triple c's
            console.log("2: ");
            return admin.database().ref('searchface/'+bizid).once('value');
        }).then(snap => {
            //set the x
            var category = snap.val().category;
            var city = snap.val().city;
            var country = snap.val().country;
            var citycountry = city.toLowerCase() + "-" + country.toLowerCase();
            console.log("1: " +citycountry+" "+ category);
            return admin.database().ref('/locations/'+citycountry+'/'+category+'/'+bizid+'/s').transaction(function (post) {
                console.log("Business s Increased: "+ post);
                return (post || 0)+1;
            });
        }).catch(reason => {
            //error
            console.log("COULD NOT SET BUSINESS SATISFIED: "+ reason);
        });
    });

//////////////////////////////////////////////////////////////////////////////////////////////////////////

exports.makeUnConversion = functions.database.ref('/cusbusinesses/{cusId}/notsatisfied/{bizId}')
    .onCreate((change, context) => {
        // Remove the user from satisfied and s
        var bizid = context.params.bizId;
        var uid = context.params.cusId;

        //user is already a bizcustomer

        return admin.database().ref('/businessinfo/'+bizid+'/satisfied').transaction(post => {
            console.log("Business satisfied Reduced: "+ post);
            return (post || 1)-1;
        }).then(snap => {
            //get the triple c's
            console.log("2: ");
            return admin.database().ref('searchface/'+bizid).once('value');
        }).then(snap => {
            //set the x
            var category = snap.val().category;
            var city = snap.val().city;
            var country = snap.val().country;
            var citycountry = city.toLowerCase() + "-" + country.toLowerCase();
            console.log("1: " +citycountry+" "+ category);
            return admin.database().ref('/locations/'+citycountry+'/'+category+'/'+bizid+'/s').transaction(function (post) {
                console.log("Business s Reduced: "+ post);
                return (post || 1)-1;
            });
        }).catch(reason => {
            //error
            console.log("COULD NOT (un)SET BUSINESS SATISFIED: "+ reason);
        });
    });

//////////////////////////////////////////////////////////////////////////////////////////////////////////

exports.makeTransaction = functions.database.ref('/transactions/{bizId}/{monthyear}/{postId}/customerid')
    .onCreate((change, context) => {
        // Make the user a business customer and add them to nofcustomers and x
        var uid = change.val();
        var bizid = context.params.bizId;
        var postid = context.params.postId;
        var username;
        var satisfied;

        //check if user is a bizcustomer before increasing nofcustomer
        return admin.database().ref('/bizcustomers/'+bizid+'/'+uid+'/satisfied').once('value').then(snap =>{
            //know if user is already a customer
            satisfied = snap.val();
            console.log("5: "+ snap.val());
            return admin.database().ref('/user/'+uid+'/uname').once('value');
        }).then(snap => {
            username = snap.val();
            console.log("4: "+ snap.val());

            if (satisfied == "2" || satisfied == null){
                //Increase the nofcustomers
                return admin.database().ref('/businessinfo/'+bizid+'/nofcustomers').transaction(function (post) {
                    return (post || 0)+1;
                });
            } else {
                return "Old customer(nofcustomers)";
            }

        }).then(snap => {
            //get the triple c's
            console.log("3: ");
            return admin.database().ref('searchface/'+bizid).once('value');
        }).then(snap => {
            //set the x
            var category = snap.val().category;
            var city = snap.val().city;
            var country = snap.val().country;
            var citycountry = city.toLowerCase() + "-" + country.toLowerCase();
            console.log("2: " +citycountry);

            if (satisfied == "2" || satisfied == "0" || satisfied == null){
                return admin.database().ref('/locations/'+citycountry+'/'+category+'/'+bizid+'/x').transaction(function (post) {
                    return (post || 0)+1;
                });
            } else {
                return "Old customer(locations)";
            }
        }).then(snap => {
            console.log("1: ");

            if (satisfied == "2" || satisfied == null){
                //Set bizcustomer
                return admin.database().ref('/bizcustomers/'+bizid+'/'+uid).set({
                    "customername" : username,
                    "satisfied" : "2"
                });
            } else {
                return "Old customer(bizcustomers)";
            }
        }).catch(reason => {
            //error
            console.log("COULD NOT SET BUSINESS CUSTOMER: "+ reason);
        });
    });

//////////////////////////////////////////////////////////////////////////////////////////////////////////

exports.makeUppercase = functions.database.ref('/searchface/{pushId}/category')
    .onCreate((change, context) => { 
        console.log('category triggered function');
        var cat = change.val();
        var citycountry;
        var lat;
        var long;
        console.log('saccount registered: ' + context.params.pushId + 'to category: ' + cat);
        //Get suggested stuff
        return admin.database().ref('/searchface/'+context.params.pushId).once('value').then(snap => {
            // get details
            console.log("5: " +snap);
            var city = snap.val().city;
            var country = snap.val().country;
            citycountry = city.toLowerCase() + "-" + country.toLowerCase();
            console.log("citycountry: " +snap);
            lat = parseFloat(snap.val().lat);
            long = parseFloat(snap.val().long);
            // save location here
            return new GeoFire(admin.database().ref('/locations/'+citycountry+'/'+cat)).set(context.params.pushId, [lat, long]);
        }).then(snap => {
            //set name, title and ratings
            console.log("4: ");
            return admin.database().ref('/businessinfo/'+context.params.pushId).once('value');
        }).then(snap => {
            var name = snap.val().name;
            var title = snap.val().title;
            var satisfied = snap.val().satisfied;
            var customers = snap.val().nofcustomers;
            console.log("3: " +snap.val().name);
            return admin.database().ref('/locations/'+citycountry+'/'+cat+'/'+context.params.pushId).update({
                "n" : name,
                "t" : title,
                "s" : satisfied,
                "x" : customers
            });
        }).then(snap => {
            console.log("2: ");
            return admin.database().ref('businesscount/'+citycountry+'/'+cat).once('value');
        }).then(snap =>{
            console.log("1: " + snap.val());
            if (snap.val() == null){
                console.log("Count Created");
                return admin.database().ref('businesscount/'+citycountry+'/'+cat).set(1);
            } else {
                console.log("Incremented");
                // Increase the nofbusineses
                return admin.database().ref('/businesscount/'+citycountry+'/'+cat).transaction(function (post) {
                    return (post || 0)+1;
                });
            }
        }).catch(reason => {
            //error
            console.log("COULD NOT VERIFY BUSINESS BECAUSE: "+ reason);
        });
    });

//////////////////////////////////////////////////////////////////////////////////////////////////////////

app.get('/new-access-code', (req, res) => {
    var customerid = req.params.customerid;
    var cartid     = req.params.cartid;
    // you can then look up customer and cart details in a db etc
    // I'm hardcoding an email here for simplicity
    var amountinkobo = req.params.amount;
    var email = req.params.email;

    // all fields supported by this call can be gleaned from
    // https://developers.paystack.co/reference#initialize-a-transaction
    paystack.transaction.initialize({
        email:     email,        // a valid email address
        amount:    amountinkobo, // only kobo and must be integer
        metadata:  {
            custom_fields:[
                {
                    "display_name":"Started From",
                    "variable_name":"started_from",
                    "value":"charge card backend"
                },
                {
                    "display_name":"Requested by",
                    "variable_name":"requested_by",
                    "value": req.headers['user-agent']
                },
                {
                    "display_name":"Server",
                    "variable_name":"server",
                    "value": req.headers.host
                }
            ]
        }
    }, function(error, body) {
        if(error){
            res.send({error:error});
            return;
        }
        res.send(body.data.access_code);
    });
});

app.get('/verify/:reference', (req, res) => {
    var reference = req.params.reference;
    console.log("Reference: " + reference);

    paystack.transaction.verify(reference,
        function(error, body) {
        if(error){
            res.send({error:error});
            return;
        }
        if(body.data.success){
            //authorization
            return;
        }
        res.send(body.data.gateway_response);
    });
});

app.post('/webhook', function(req, res) {
    //Triggered everytime a user is charged
    var event = req.body;
    var reference = event.data.reference;
    console.log(reference);
    if (event.event == "charge.success"){
        var cid = reference.split("_T_");
        var autho = event.data.authorization;
        admin.database().ref('/user/'+cid[0]+'/chargeauthcode/paystackng').set(autho);
        console.log(cid[0]+" charged successfully");
    } else {
        console.log("charge failed");
    }
    res.status(200).send("paystack webhook");
});

//The 404 Route
app.get('/*', (req, res) => {
    res.status(404).send('👁');
});

app.listen(3002);

//////////////////////////////////////////////////////////////////////////////////////////////////////////

appy.get('/b', (req, res) => {
    res.send("You should go to the home page");
});

appy.get('/b/:id', (req, res) => {
    res.set('Cache-Control', 'public, max-age=43200, s-maxage=300');
    var id = req.params.id;
    var businessid = {};
    var bizobject = {};
    var businessinfo = {};
    var inventory = {};
    var reviews = {};
    //get businessid
    admin.database().ref('businessid/'+id).once('value').then(snap =>{
        businessid = {"text":snap.val(), "link":id};
        return admin.database().ref('businessinfo/'+businessid.text).once('value');
    }).then(snap => {
        businessinfo = snap.val();
        console.log(businessinfo);
        return admin.database().ref('bizinventory/'+businessid.text).once('value');
    }).then(snap => {
        inventory = snap.val();
        console.log(inventory);
        return admin.database().ref('reviews/'+businessid.text).orderByChild('time').limitToFirst(3).once('value');
    }).then(snap => {
        reviews = snap.val();
        bizobject = {"id":businessid, "businessinfo":businessinfo, "inventory":inventory, "reviews":reviews};
        //loop through reviews object and recalculate time in millsec to days
        for (cid in bizobject.reviews){
            console.log("time: "+bizobject.reviews[cid].time)
            var days = Math.round(bizobject.reviews[cid].time/86400000000);
            if(days < 1){
                bizobject.reviews[cid].time = "today";
            } else if (days == 1){
                bizobject.reviews[cid].time = "yesterday";
            } else {
                bizobject.reviews[cid].time = days+" days ago";
            }
        }
        //in future, pick the page template based on the business' country
        res.render('business', {bizobject});
    }).catch(reason => {
        //error
        console.log("COULD NOT GET BUSINESS: "+ reason);
    });
});

//The 404 Route
appy.get('/*', (req, res) => {
    res.status(404).send('404');
});

appy.listen(3000);

//////////////////////////////////////////////////////////////////////////////////////////////////////////

appier.get('/i', (req, res) => {
    res.send("You should go to the home page");
});

appier.get('/i/:itid', (req, res) => {
    res.set('Cache-Control', 'public, max-age=43200, s-maxage=300');
    var itid = req.params.itid;
    var itemid = itid.split('-by-')[0];
    var id = itid.split('-by-')[1];
    var name = id.split('-').join(' ');
    var businessid = {};
    var itemobject = {};
    var iteminfo = {};
    //get businessid
    admin.database().ref('businessid/'+id).once('value').then(snap =>{
        businessid = {"text":snap.val(), "link":id, "name":name};
        console.log("1:"+businessid);
        return admin.database().ref('businessinfo/'+businessid.text).once('value');
    }).then(snap => {
        businessinfo = snap.val();
        console.log("2:"+businessinfo);
        return admin.database().ref('bizinventory/'+businessid.text+'/'+itemid).once('value');
    }).then(snap => {
        iteminfo = snap.val();
        console.log("3:"+iteminfo);
        itemobject = {"itemid":itemid, "businessid":businessid, "iteminfo":iteminfo};
        res.render('item', {itemobject});
    }).catch(reason => {
        //error
        console.log("COULD NOT GET ITEM: "+ reason);
    });
});

//The 404 Route
appier.get('/*', (req, res) => {
    res.status(404).send('404');
});

appier.listen(3001);

//////////////////////////////////////////////////////////////////////////////////////////////////////////

exports.app = functions.https.onRequest(app);
exports.appy = functions.https.onRequest(appy);
exports.appier = functions.https.onRequest(appier);