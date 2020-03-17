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

class TestFridge {
	var fridge : ActorBasic? = null
	var mqttClient : MqttClient? = null
	var broker : String = "tcp://localhost"
	var rbrTopic : String = "unibo/qak/roombutlerrobot"
	var eventTopic : String = "unibo/qak/events"
	var clientId : String = "sprint_3"
    var qos : Int = 2;
	
	@Before
	fun systemSetUp() {

		try {
			println("%%%%%%%%%%%%%% Sprint 3 TestFridge starting Mqtt Client")
			mqttClient = MqttClient(broker, clientId)
			var connOpts = MqttConnectOptions()
			connOpts.setCleanSession(true)
			mqttClient!!.connect(connOpts)
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
		println("%%%%%%% Sprint 3 TestFridge terminate ")
		try {
			mqttClient!!.disconnect()
			mqttClient!!.close()
		} catch (e : MqttException) {
			println("MQTT EXCEPTION IN TERMINATE")
		}
	}
	
	@Test
	fun sprint3Test() {
		println("%%%%%%% Sprint 3 Functional TestFridge starts ")
		prepare()
		addFoodFail("fruit")
		addFoodOk("beef")
		clear()
	}
	
	fun prepare() {
		println("%%%%%%% Sprint 3 TestFridge prepareRoom Task")
		sendCmd("prepare", "")
		solveCheckGoal(fridge!!, "fridge(state([beef]))")
	}
	
	fun addFoodFail(foodCode : String) {
		println("%%%%%%% Sprint 3 TestFridge addFood Task")
		sendCmd("addFood", foodCode)
		solveCheckGoal(fridge!!, "fridge(state([beef]))")
		solveCheckGoal(fridge!!, "no")
	}

	fun addFoodOk(foodCode : String) {
		println("%%%%%%% Sprint 3 Test addFood Task")
		sendCmd("addFood", foodCode)
		solveCheckGoal(fridge!!, "yes")
		solveCheckGoal(fridge!!, "fridge(state([]))")
	}
	
	fun clear() {
		println("%%%%%%% Sprint 3 TestFridge clearRoom Task")
		sendCmd("clear", "")
		solveCheckGoal(fridge!!, "fridge(state([fruit,beef]))")
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
	
	fun solveCheckGoal( actor: ActorBasic, goal : String ){
		var result : Boolean
		if(goal.equals("yes") || goal.equals("no")) {
			result = (goal == itunibo.coap.fridgeResourceCoap.getAnswer())
		} else {
			println(itunibo.coap.fridgeResourceCoap.getModel())
			result = (goal == itunibo.coap.fridgeResourceCoap.getModel())
		}
		println(" %%%%%%%  actor={$actor.name} goal= $goal  result = $result")
		assertTrue(result)
	}
	
	fun delay( time : Long ){
		Thread.sleep( time )
	}
}