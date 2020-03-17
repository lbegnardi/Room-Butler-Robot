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
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken

class TestFridge {
	var fridge : ActorBasic? = null
	var mqttClient : MqttClient? = null
	var broker : String = "tcp://localhost"
	var fridgeTopic : String = "unibo/qak/fridge"
	var eventTopic : String = "unibo/qak/events"
	var fridgeAns : String = ""
	var clientId : String = "sprint_2"
    var qos : Int = 2;
	
	val eventCallback = object : MqttCallback {
		override fun connectionLost(cause: Throwable) {
			//connectionStatus = false
			// Give your callback on failure here
		}
		override fun messageArrived(topic: String, message: MqttMessage) {
			try {
				val data = String(message.payload, charset("UTF-8"))
				// data is the desired received message
				// Give your callback on message received here
				fridgeAns = data
			} catch (e: Exception) {
				// Give your callback on error here
			}
		}
		override fun deliveryComplete(token: IMqttDeliveryToken) {
			// Acknowledgement on delivery complete
		}
	}
	
	@Before
	fun systemSetUp() {

		try {
			println("%%%%%%%%%%%%%% Sprint 2 TestFridge starting Mqtt Client")
			mqttClient = MqttClient(broker, clientId)
			var connOpts = MqttConnectOptions()
			connOpts.setCleanSession(true)
			mqttClient!!.connect(connOpts)
			mqttClient!!.setCallback(eventCallback)			
			mqttClient!!.subscribe(eventTopic)
		}
		catch (e : MqttException) {
			println("MQTT EXCEPTION IN SETUP")
		}
		GlobalScope.launch {
			it.unibo.ctxFridge.main()
		}
		delay(5000) //give the time to start
		fridge = sysUtil.getActor("fridge")
 	}
 
	@After
	fun terminate() {
		println("%%%%%%% Sprint 2 TestFridge terminate ")
		try {
			mqttClient!!.disconnect()
			mqttClient!!.close()
		} catch (e : MqttException) {
			println("MQTT EXCEPTION IN TERMINATE")
		}
	}
	
	@Test
	fun sprint2Test() {
		println("%%%%%%% Sprint 2 Functional TestFridge starts ")
		expose()
		consultOk("fruit")
		consultFail("pasta")
	}
	
	fun expose() {
		println("%%%%%%% Sprint 2 TestFridge expose Task")
		sendCmd("expose", "")
		delay(1000)
		solveCheckGoal(fridge!!, fridgeAns, "msg(fridgeContent,event,fridge,none,content(fridge(state([fruit,beef]))),6)")
	}
	
	fun consultOk(foodCode : String) {
		println("%%%%%%% Sprint 2 TestFridge consultOk Task")
		sendCmd("request", "fridge, $foodCode")
		delay(1000)
		solveCheckGoal(fridge!!, fridgeAns, "msg(answer,event,fridge,none,answer(content(yes)),7)")
	}

	fun consultFail(foodCode : String) {
		println("%%%%%%% Sprint 2 TestFridge consultOFail Task")
		sendCmd("request", "fridge, $foodCode")
		delay(1000)
		solveCheckGoal(fridge!!, fridgeAns, "msg(answer,event,fridge,none,answer(content(no)),8)")
	}

//-------------------------------------------------------------------------------

	fun sendCmd(cmd : String, content : String) {
		println("--- Fridge performing performing task $cmd")
		var msg : String
		if (content != "") {
			msg = "msg($cmd,dispatch,js,fridge,$cmd($content),1)"
		} else {
			msg = "msg($cmd,dispatch,js,fridge,$cmd,1)"
		}
		try {
			var mqttMsg = MqttMessage(msg.toByteArray())
			mqttClient!!.publish(fridgeTopic, mqttMsg)
		}
		catch (e : MqttException) {
			println("MQTT EXCEPTION IN DOTASK")
		}
	}
	
	fun solveCheckGoal( actor : ActorBasic, goal : String ){
		actor.solve( goal )
		var result =  actor.resVar
		println(" %%%%%%%  actor={$actor.name} goal= $goal  result = $result")
		assertTrue("", result == "success" )
	}
	
	fun solveCheckGoal( actor : ActorBasic, ans : String, goal : String ){
		println(" %%%%%%%  actor={$actor.name} goal= $goal  ans= $ans")
		assertTrue("", ans == goal )
	}
	
	fun delay( time : Long ){
		Thread.sleep( time )
	}
}