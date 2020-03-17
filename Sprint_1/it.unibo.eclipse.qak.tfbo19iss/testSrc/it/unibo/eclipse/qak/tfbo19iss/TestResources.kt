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

class TestResources {
	var resource : ActorBasic? = null
	var mqttClient : MqttClient? = null
	var broker : String = "tcp://localhost"
	var rbrTopic : String = "unibo/qak/roombutlerrobot"
	var clientId : String = "sprint_1"
    var qos : Int = 2;
	
	@Before
	fun systemSetUp() {

		try {
			println("%%%%%%%%%%%%%% Sprint 1 TestResources starting Mqtt Client")
			mqttClient = MqttClient(broker, clientId)
			var connOpts = MqttConnectOptions()
			connOpts.setCleanSession(true)
			mqttClient!!.connect(connOpts)
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
		println("%%%%%%% Sprint 1 TestResources terminate ")
		try {
			mqttClient!!.disconnect()
			mqttClient!!.close()
		} catch (e : MqttException) {
			println("MQTT EXCEPTION IN TERMINATE")
		}
	}
	
	@Test
	fun sprint1Test() {
		println("%%%%%%% Sprint 1 Functional TestResources starts ")
		prepare()
		clear()
	}
	
	fun prepare() {
		println("%%%%%%% Sprint 1 TestResources prepareRoom Task")
		sendCmd("prepare", "")
		solveCheckGoal(resource!!, "model( actuator, robot, state( stopped ) )")
		solveCheckGoal(resource!!, "model( resource, pantry, state([ dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish ]) )")
		solveCheckGoal(resource!!, "model( resource, table, state([ fruit, dish ]) )")
		solveCheckGoal(resource!!, "model( resource, dishwasher, state([ ]) )")
	}
	
	fun clear() {
		println("%%%%%%% Sprint 1 TestResources clearRoom Task")
		sendCmd("clear", "")
		solveCheckGoal(resource!!, "model( actuator, robot, state( stopped ) )")
		solveCheckGoal(resource!!, "model( resource, pantry, state([ dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish, dish ]) )")
		solveCheckGoal(resource!!, "model( resource, table, state([ ]) )")
		solveCheckGoal(resource!!, "model( resource, dishwasher, state([ dish ]) )")
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
	
	fun solveCheckGoal( actor : ActorBasic, goal : String ){
		actor.solve( goal )
		var result =  actor.resVar
		println(" %%%%%%%  actor={$actor.name} goal= $goal  result = $result")
		assertTrue("", result == "success" )
	}
	
	fun delay( time : Long ){
		Thread.sleep( time )
	}
}