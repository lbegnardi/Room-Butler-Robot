package it.unibo.eclipse.qak.tfbo19iss

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import alice.tuprolog.SolveInfo
import it.unibo.kactor.sysUtil
import it.unibo.kactor.ActorBasic
import it.unibo.kactor.MsgUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttException

class TestRBR {
	var rbr : ActorBasic? = null
	var mqttClient : MqttClient? = null
	var broker : String = "tcp://localhost"
	var rbrTopic : String = "unibo/qak/roombutlerrobot"
	var clientId : String = "sprint_1"
    var qos : Int = 2;
	
	@Before
	fun systemSetUp() {

		try {
			println("%%%%%%%%%%%%%% Sprint 1 TestRBR starting Mqtt Client")
			mqttClient = MqttClient(broker, clientId)
			var connOpts = MqttConnectOptions()
			connOpts.setCleanSession(true)
			mqttClient!!.connect(connOpts)
		}
		catch (e : MqttException) {
			println("MQTT EXCEPTION IN SETUP")
		}
		GlobalScope.launch {
			it.unibo.ctxRBR.main()
		}
		delay(5000) //give the time to start
		rbr = sysUtil.getActor("roombutlerrobot")
 	}
 
	@After
	fun terminate() {
		println("%%%%%%% Sprint 1 TestRBR terminate ")
		try {
			mqttClient!!.disconnect()
			mqttClient!!.close()
		} catch (e : MqttException) {
			println("MQTT EXCEPTION IN TERMINATE")
		}
	}
	
	@Test
	fun sprint1Test() {
		println("%%%%%%% Sprint 1 Functional TestRBR starts ")
		prepare()
		addFoodFail("fruit")
		addFoodOk("beef")
		clear()
	}
	
	fun prepare() {
		println("%%%%%%% Sprint 1 TestRBR prepareRoom Task")
		sendCmd("prepare", "")
		solveCheckGoal(rbr!!, 0, 0)
	}
	
	fun addFoodFail(foodCode : String) {
		println("%%%%%%% Sprint 1 TestRBR addFood Task")
		sendCmd("addFood", foodCode)
		solveCheckGoal(rbr!!, 0, 0)
	}

	fun addFoodOk(foodCode : String) {
		println("%%%%%%% Sprint 1 TestRBR addFood Task")
		sendCmd("addFood", foodCode)
		solveCheckGoal(rbr!!, 0, 0)
	}
	
	fun clear() {
		println("%%%%%%% Sprint 1 TestRBR clearRoom Task")
		sendCmd("clear", "")
		solveCheckGoal(rbr!!, 0, 0)
	}

//-------------------------------------------------------------------------------

	fun sendCmd(cmd : String, content : String) {
		println("--- RBR performing performing task $cmd")
		var msg : String
		if (content != "") {
			msg = "msg($cmd,dispatch,js,roombutlerrobot,$cmd($content),1)"
		} else {
			msg = "msg($cmd,dispatch,js,roombutlerrobot,$cmd,1)"
		}
		try {
			var mqttMsg = MqttMessage(msg.toByteArray())
			mqttClient!!.publish(rbrTopic, mqttMsg)
		}
		catch (e : MqttException) {
			println("MQTT EXCEPTION IN DOTASK")
		}
		if(content == "fruit") {
			delay(5000)
		}
		else {
			delay(45000) //wait 45 seconds to check the results
		}
	}
	
	fun solveCheckGoal( actor : ActorBasic, x : Int, y : Int ){
		var result =  itunibo.planner.moveUtils.getPosX(actor) == x && itunibo.planner.moveUtils.getPosY(actor) == y
		println(" %%%%%%%  actor={$actor.name} goal = RBR in ($x,$y) result = $result")
		assertTrue(result)
	}
	
	fun delay( time : Long ){
		Thread.sleep( time )
	}
}