/*
frontend/uniboSupports/coapClientToFridge
*/
const coap             = require("node-coap-client").CoapClient;
var coapAddr       = "coap://localhost:5683"
var coapResourceAddr   = coapAddr + "/fridge"
var io; 	//Upgrade for socketIo;
/*
coap
    .tryToConnect( coapAddr )
    .then((result ) => { //  true or error code or Error instance  
        console.log("coap connection done"); // do something with the result  
    })
    ;
*/

exports.setIoSocket = function (iosock) {
    io = iosock;
    console.log("coap SETIOSOCKET io=" + io);
}

exports.setcoapAddr = function ( addr ){
	coapAddr = "coap://"+ addr + ":5683";
	coapResourceAddr = coapAddr + "/fridge";
	console.log("coap coapResourceAddr  " + coapResourceAddr);
}

exports.coapGet = function ( param ){
	coap
	    .request(
	        coapResourceAddr,
	        "get",                                  //"get" | "post" | "put" | "delete"
            new Buffer(param)                       //payload Buffer
	        //[options]]                            // RequestOptions
	    )
	    .then(response => { 			/* handle response */
            var msgStr = response.payload
	        console.log("coap get done> " + response.payload);
	        if (msgStr.indexOf("fridge") < 0) {
	            if (msgStr == "yes") {
	                content = "Answer: The fridge contains the requested food.";
	            } else {
	                content = "Answer: The fridge does not contain the requested food.";
	            }
	        } else
	            content = "Fridge exposing its content: " + msgStr;

	        console.log("coap send on io.sockets| content=" + content);
	        io.sockets.send(content);

	    })
	    .catch(err => { /* handle error */ 
	    	console.log("coap get error> " + err );}
	    )
	    ;
	    
}//coapPut

exports.coapPut = function (  cmd ){ 
	coap
	    .request(
	        coapResourceAddr,     
	        "put" ,			                          // "get" | "post" | "put" | "delete"   
	        new Buffer(cmd )                          // payload Buffer 
 	        //[options]]							//  RequestOptions 
	    )
	    .then(response => { 			// handle response
            
	    	console.log("coap put done> " + cmd);}
	     )
	    .catch(err => { // handle error  
	    	console.log("coap put error> " + err + " for cmd=" + cmd);}
	    )
	    ;
	    
}//coapPut

const myself          = require('./coapClientToFridge');

//test()

/*
 * ========= EXPORTS =======
 */

//module.exports = coap;