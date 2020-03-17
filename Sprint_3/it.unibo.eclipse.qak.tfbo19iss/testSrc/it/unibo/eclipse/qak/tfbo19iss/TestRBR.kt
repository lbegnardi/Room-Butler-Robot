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
	var planexec : ActorBasic? = null
	var mqttClient : MqttClient? = null
	var broker : String = "tcp://localhost"
	var rbrTopic : String = "unibo/qak/roombutlerrobot"
	var eventTopic : String = "unibo/qak/events"
	var clientId : String = "sprint_3"
    var qos : Int = 2;
	
	@Before
	fun systemSetUp() {

		try {
			println("%%%%%%%%%%%%%% Sprint 3 TestRBR starting Mqtt Client")
			mqttClient = MqttClient(broker, clientId)
			var connOpts = MqttConnectOptions()
			connOpts.setCleanSession(true)
			mqttClient!!.connect(connOpts)
		} catch (e : MqttException) {
			println("MQTT EXCEPTION IN SETUP")
		}
		GlobalScope.launch {
			it.unibo.ctxRBR.main()
		}
		delay(5000) //give the time to start
		rbr = sysUtil.getActor("roombutlerrobot")
		//planexec = sysUtil.getActor("plaexecutor")
 	}
 
	@After
	fun terminate() {
		println("%%%%%%% Sprint 3 TestRBR terminate ")
		try {
			mqttClient!!.disconnect()
			mqttClient!!.close()
		} catch (e : MqttException) {
			println("MQTT EXCEPTION IN TERMINATE")
		}
	}
	
	@Test
	fun sprint3Test() {
		println("%%%%%%% Sprint 3 Functional TestRBR starts ")
		stop()
		reactivate()
		avoidObstacle()
	}
	
	fun stop() {
		println("%%%%%%% Sprint 3 TestRBR stop command")
		sendCmd("prepare", "")
		delay(5000)
		emitEvent("stop", "")
		delay(5000)
		solveCheckGoal(rbr!!, 0, 0, false)
	}
	
	fun reactivate() {
		println("%%%%%%% Sprint 3 TestRBR reactivate command")
		emitEvent("resume", "")
		delay(40000)
		solveCheckGoal(rbr!!, 0, 0, true)
	}

	fun avoidObstacle() {
		println("%%%%%%% Sprint 3 TestRBR reactivate command")
		sendCmd("addFood", "beef")
		delay(1500)
		emitEvent("obstacle", "5")
		delay(45000)
		solveCheckGoal(rbr!!, 0, 0, true)
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
		} catch (e : MqttException) {
			println("MQTT EXCEPTION IN DOTASK")
		}
	}
	
	fun emitEvent(cmd : String, content : String) {
		println("--- RBR performing performing cmd $cmd")
		var msg : String
		if (content != "") {
			msg = "msg($cmd,event,js,none,$cmd($content),1)"
		} else {
			msg = "msg($cmd,event,js,none,$cmd,1)"
		}
		try {
			var mqttMsg = MqttMessage(msg.toByteArray())
			mqttClient!!.publish(eventTopic, mqttMsg)
		} catch (e : MqttException) {
			println("MQTT EXCEPTION IN DOTASK")
		}
	}
	
	fun solveCheckGoal( actor : ActorBasic, x : Int, y : Int, goal : Boolean ) {
		if(goal) {
			var result =  itunibo.planner.moveUtils.getPosX(actor) == x && itunibo.planner.moveUtils.getPosY(actor) == y
			println(" %%%%%%%  actor={$actor.name} goal = RBR in ($x,$y) result = $result")
			assertTrue(result)
			
		} else {
			var result =  itunibo.planner.moveUtils.getPosX(actor) != x && itunibo.planner.moveUtils.getPosY(actor) != y
			println(" %%%%%%%  actor={$actor.name} goal = RBR not in ($x,$y) result = $result")
			assertTrue(result)
		}
	}	
	
	fun delay( time : Long ){
		Thread.sleep( time )
	}
}