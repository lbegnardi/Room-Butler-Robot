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

class TestResources {
	var resource : ActorBasic? = null
	var mqttClient : MqttClient? = null
	var broker : String = "tcp://localhost"
	var resmodelTopic : String = "unibo/qak/resourcemodel"
	var eventTopic : String = "unibo/qak/events"
	var resStates = ArrayList<String>()
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
				resStates.add(data)
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
			println("%%%%%%%%%%%%%% Sprint 2 TestResources starting Mqtt Client")
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
			it.unibo.ctxRobotMind.main()
		}
		delay(5000) //give the time to start
		resource = sysUtil.getActor("resourcemodel")
 	}
 
	@After
	fun terminate() {
		println("%%%%%%% Sprint 2 TestResources terminate ")
		try {
			mqttClient!!.disconnect()
			mqttClient!!.close()
		} catch (e : MqttException) {
			println("MQTT EXCEPTION IN TERMINATE")
		}
	}
	
	@Test
	fun sprint1Test() {
		println("%%%%%%% Sprint 2 Functional TestResources starts ")
		expose()
	}
	
	fun expose() {
		println("%%%%%%% Sprint 2 TestResources expose Task")
		sendCmd("modelExpose", "")
		delay(1000)
		solveCheckGoal(resource!!, resStates.get(0), "msg(modelContent,event,resourcemodel,none,content(robot(state(stopped))),16)")
		solveCheckGoal(resource!!, resStates.get(1), "msg(modelContent,event,resourcemodel,none,content(roomMap(state(unknown))),17)")
		solveCheckGoal(resource!!, resStates.get(2), "msg(modelContent,event,resourcemodel,none,content(table(state([]))),18)")
		solveCheckGoal(resource!!, resStates.get(3), "msg(modelContent,event,resourcemodel,none,content(pantry(state([dish,dish,dish,dish,dish,dish,dish,dish,dish,dish,dish,dish,dish,dish,dish]))),19)")
		solveCheckGoal(resource!!, resStates.get(4), "msg(modelContent,event,resourcemodel,none,content(dishwasher(state([]))),20)")
	}
	
//-------------------------------------------------------------------------------

	fun sendCmd(cmd : String, content : String) {
		println("--- RBR performing performing task $cmd")
		var msg : String
		if (content != "") {
			msg = "msg($cmd,dispatch,js,resourcemodel,$cmd($content),1)"
		} else {
			msg = "msg($cmd,dispatch,js,resourcemodel,$cmd,1)"
		}
		try {
			var mqttMsg = MqttMessage(msg.toByteArray())
			mqttClient!!.publish(resmodelTopic, mqttMsg)
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