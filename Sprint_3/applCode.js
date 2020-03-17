/*
frontend/applCode
*/
const express     	= require('express');
const path         	= require('path');
//const favicon     = require('serve-favicon');
const logger       	= require('morgan');	//see 10.1 of nodeExpressWeb.pdf;
//const cookieParser= require('cookie-parser');
const bodyParser   	= require('body-parser');
const fs           	= require('fs');
const index         = require('./appServer/routes/index');				 
var io              ; 	//Upgrade for socketIo;

//for delegate
const mqttUtils     = require('./uniboSupports/mqttUtils');  
const coap          = require('./uniboSupports/coapClientToFridge');  

var app              = express();

// view engine setup;
app.set('views', path.join(__dirname, 'appServer', 'views'));	 
app.set('view engine', 'ejs');

//create a write stream (in append mode) ;
var accessLogStream = fs.createWriteStream(path.join(__dirname, 'morganLog.log'), {flags: 'a'})
app.use(logger("short", {stream: accessLogStream}));

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));				//shows commands, e.g. GET /pi 304 23.123 ms - -;
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
//app.use(cookieParser());

app.use(express.static(path.join(__dirname, 'public')));
app.use(express.static(path.join(__dirname, 'jsCode'))); //(***)

app.get('/', function(req, res) {
    res.render("index");
    console.log("starting")
    setTimeout(delegateForResource, 200, "modelExpose", req, res);
});

/*
 * ====================== COMMANDS ================
 */
//TESTING
    app.post("/changePrepSet", function (req, res, next) {
        content = req.body.prep_set;
        delegateForAppl("prepChange", req, res, content);
        next();
    });
    app.post("/addFridge", function (req, res, next) {
        content = "add(" + req.body.foodcode_resfridge + ")";
        delegateForFridge("addFridge", req, res, content);
        next();
    });
    app.post("/removeFridge", function (req, res, next) {
        content = "remove(" + req.body.foodcode_resfridge + ")";
        delegateForFridge("removeFridge", req, res, content);
        next();
    });
    app.post("/addTable", function (req, res, next) {
        content = "table, add(" + req.body.itemcode_table + ")";
        delegateForResource("modelUpdate", req, res, content);
        next();
    });
    app.post("/removeTable", function (req, res, next) {
        content = "table, remove(" + req.body.itemcode_table + ")";
        delegateForResource("modelUpdate", req, res, content);
        next();
    });
    app.post("/addPantry", function (req, res, next) {
        content = "pantry, add(" + req.body.itemcode_pantry + ")";
        delegateForResource("modelUpdate", req, res, content);
        next();
    });
    app.post("/removePantry", function (req, res, next) {
        content = "pantry, remove(" + req.body.itemcode_pantry + ")";
        delegateForResource("modelUpdate", req, res, content);
        next();
    });
    app.post("/addDishwasher", function (req, res, next) {
        content = "dishwasher, add(" + req.body.itemcode_dishwasher + ")";
        delegateForResource("modelUpdate", req, res, content);
        next();
    });
    app.post("/removeDishwasher", function (req, res, next) {
        content = "dishwasher, remove(" + req.body.itemcode_dishwasher + ")";
        delegateForResource("modelUpdate", req, res, content);
        next();
    });

  	//APPLICATION
	app.post("/stop", function(req, res,next) {
  		delegateForEvents("stop", req, res);
  		next();
 	});		
	app.post("/reactivate", function(req, res,next) {
  		delegateForEvents("resume",  req, res);
  		next();
 	});		
	app.post("/explore", function(req, res,next) {
	    delegateForAppl("explore", req, res);
  		next();
 	});		
	app.post("/prepare", function(req, res,next) {
	    delegateForAppl("prepare", req, res);
  		next();
 	});		
	app.post("/clear", function(req, res,next) {
	    delegateForAppl("clear", req, res);
  		next();
 	});		
	app.post("/addFood", function (req, res, next) {
        content = req.body.foodcode_app
        delegateForAppl("addFood", req, res, content);
	    next();
	});
	app.post("/expose", function (req, res, next) {
	    delegateForFridge("expose", req, res, "");
	    next();
	});
	app.post("/ask", function (req, res, next) {
	    food = req.body.foodcode_fridge
	    //getFridgeModelCoap(food)
	    delegateForFridge("ask", req, res, food);
	    next();
	});

//=================== UTILITIES =========================

var result = "";

app.setIoSocket = function( iosock ){
 	io    = iosock;
 	mqttUtils.setIoSocket(iosock);
 	coap.setIoSocket(iosock);
	console.log("app SETIOSOCKET io=" + io);
}

function delegateForEvents(cmd, req, res) {
    console.log("app delegateForEvents cmd=" + cmd);
    result = "Web server delegateForEvents: " + cmd;

    emitEvent(cmd);
}

function delegateForAppl(cmd, req, res, content) {
    console.log("app delegateForAppl cmd=" + cmd); 
    result = "Web server delegateForAppl: " + cmd;
    
    if (arguments.length === 4) {
        publishMsgToRobotapplication(cmd, content);
    }
    else {
        publishMsgToRobotapplication(cmd);
    }
}

function delegateForResource(cmd, req, res, content) {
    console.log("app delegateForResourceModel cmd=" + cmd);
    result = "Web server delegateForResourceModel: " + cmd;

    if (arguments.length === 4) {
        publishMsgToResourceModel(cmd, content);
    }
    else {
        publishMsgToResourceModel(cmd);
    }
}

function delegateForFridge(cmd, req, res, content) {
    console.log("app delegateForFridge cmd=" + cmd);
    result = "Web server delegateForFridge: " + cmd;

    if (cmd == "expose" || cmd == "ask") {
        console.log(content);
        getFridgeModelCoap(content);
    }
    else {
        console.log(content)
        changeFridgeModelCoap(content);
    }
}

/*
 * ============ TO THE BUSINESS LOGIC =======
 */

var publishMsgToRobotapplication = function (cmd, content) {
    var msgstr;
    if (arguments.length === 2) {
        msgstr = "msg(" + cmd + ",dispatch,js,roombutlerrobot," + cmd + "(" + content + "),1)";
    } else {
        msgstr = "msg(" + cmd + ",dispatch,js,roombutlerrobot," + cmd + ",1)";
    }
    console.log("publishMsgToRobotapplication/" + arguments.length +" forward> " + msgstr);
    mqttUtils.publish(msgstr, "unibo/qak/roombutlerrobot");
}

var publishMsgToResourceModel = function (cmd, content) {
    var msgstr;
    if (arguments.length === 2) {
        msgstr = "msg(" + cmd + ",dispatch,js,resourcemodel," + cmd + "(" + content + "),1)";
    } else {
        msgstr = "msg(" + cmd + ",dispatch,js,resourcemodel," + cmd + ",1)";
    }
    console.log("publishMsgToResourceModel/" + arguments.length + " forward> " + msgstr);
    mqttUtils.publish(msgstr, "unibo/qak/resourcemodel");
}

var emitEvent = function (cmd) {
    var msgstr;
    msgstr = "msg(" + cmd + ",event,js,none," + cmd + ",1)";

    console.log("emitEvent/ forward> " + msgstr);
    mqttUtils.publish(msgstr, "unibo/qak/events");
}

var getFridgeModelCoap = function (param) {
    console.log("coap GET> ");
    coap.coapGet(param);    //see fridgeResourceCoap
}

var changeFridgeModelCoap = function (cmd) {
    console.log("coap PUT> " + cmd);
    coap.coapPut(cmd);	    //see fridgeResourceCoap
}

/*
* ====================== REPRESENTATION ================
*/
app.use( function(req,res){
	console.info("SENDING THE ANSWER " + result + " json:" + req.accepts('josn') );
	try{
	    console.log("answer> "+ result  );
	    /*
	   if (req.accepts('json')) {
	       return res.send(result);		//give answer to curl / postman
	   } else {
	       return res.render('index' );
	   };
	   */
	   //res.send(result);
	   //return res.render('index' );  //NO: we loose the message sent via socket.io
	}catch(e){console.info("SORRY ..." + e);}
	} 
);

//app.use(converter());

/*
 * ============ ERROR HANDLING =======
 */

// catch 404 and forward to error handler;
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler;
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page;
  res.status(err.status || 500);
  res.render('error');
});

/*
 * ========= EXPORTS =======
 */

module.exports = app;