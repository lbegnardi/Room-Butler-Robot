/*
* =====================================
* frontend/uniboSupports/mqttUtils.js
* =====================================
*/
const mqtt   = require ('mqtt');  //npm install --save mqtt
const topic  = "unibo/qak/events";

var mqttAddr = 'mqtt://localhost'

var client   = mqtt.connect(mqttAddr);
var io  ; 	//Upgrade for socketIo;
var robotModel    = "none";
var sonarModel    = "none";
var roomMapModel  = "none";

console.log("mqtt client= " + client );

exports.setIoSocket = function ( iosock ) {
 	io    = iosock;
	console.log("mqtt SETIOSOCKET io=" + io);
}


client.on('connect', function () {
	  client.subscribe( topic );
	  console.log('client has connected successfully with ' + mqttAddr);
});

client.on('message', function (topic, message){
  //console.log("mqtt io="+ io );
  //msg(modelContent,event,resourcemodel,none,content(robot(state(5))),74)
  console.log("mqtt RECEIVES:" + message.toString());
  var msgStr = message.toString();
  if(msgStr.indexOf("content")<0) return; 		//it is some other message sent via MQTT
  var spRobot         = msgStr.indexOf("robot");
  var spSonarRobot    = msgStr.indexOf("sonarRobot");
  var spRoomMap       = msgStr.indexOf("roomMap");
  var spTable         = msgStr.indexOf("table");
  var spPantry        = msgStr.indexOf("pantry");
  var spDishwasher    = msgStr.indexOf("dishwasher");
  var spWarning       = msgStr.indexOf("warning");
  var spFridgeContent = msgStr.indexOf("fridgeContent");
  var spAnswer        = msgStr.indexOf("answer");
  var sp1 = msgStr.indexOf("state");
  var msgStr = msgStr.substr(sp1);
  var sp2 = msgStr.indexOf("))");
  var msg = "";
  if (spWarning >= 0) {
      content = "The fridge does not contain the selected food.";
      msg = msg + "Warning: ";
      warning = msg + content;
  } else if (spAnswer >= 0) {
      msgStr = message.toString()
      sp1 = msgStr.indexOf("content");
      msgStr = msgStr.substr(sp1);
      var sp2 = msgStr.indexOf("))");
      var content = message.toString().substr(sp1, sp2 + 1);
      msg = msg + "Answer: "
      if (content == "content(yes)") {
          content = "The fridge contains the requested food.";
      } else {
          content = "The fridge does not contain the requested food.";
      }
      answer = msg + content;
  } else if (spFridgeContent >= 0) {
      var content = message.toString().substr(sp1, sp2 + 1);
      msg = msg + "Fridge exposing its content: ";
      fridgeModel = msg + content;
  } else {
      var content = message.toString().substr(sp1, sp2 + 1);
      if (spRobot > 0) { msg = msg + "robotState:"; robotModel = msg + content; };
      if (spSonarRobot > 0) { msg = msg + "sonarRobot:"; sonarModel = msg + content; };
      if (spRoomMap > 0) { msg = msg + "roomMap:"; roomMapModel = msg + content; };
      if (spTable > 0) { msg = msg + "table:"; tableModel = msg + content; };
      if (spPantry > 0) { msg = msg + "pantry:"; pantryModel = msg + content; };
      if (spDishwasher > 0) { msg = msg + "dishwasher:"; dishwasher = msg + content; };
  };
  msg = msg + content;
  console.log("mqtt send on io.sockets| " + msg + " content=" + content);
  io.sockets.send(msg);
});
 
exports.publish = function( msg, topic ){
	//console.log('mqtt publish ' + client);
	client.publish(topic, msg);
}