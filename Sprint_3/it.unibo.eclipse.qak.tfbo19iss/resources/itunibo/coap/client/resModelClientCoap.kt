package itunibo.coap.client

import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.CoapResponse
import org.eclipse.californium.core.Utils
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.eclipse.californium.core.CoapHandler
import org.eclipse.californium.core.coap.Request
import org.eclipse.californium.core.coap.CoAP.Code
import it.unibo.kactor.ActorBasic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import it.unibo.kactor.MsgUtil
import kotlinx.coroutines.delay

object coapClientResModel {

	private lateinit var coapClient: CoapClient
	private lateinit var coapURI: String
	private lateinit var actor : ActorBasic

	fun createClient(a: ActorBasic, serverAddr: String, port: Int, resourceName: String?) {
		actor = a
		coapClient = CoapClient("coap://$serverAddr:" + port + "/" + resourceName)
		coapURI = "coap://$serverAddr:" + port + "/" + resourceName
		println("Client started")
	}

	fun synchGet(v: String) { //Synchronously send the GET message (blocking call)
		println("%%% synchGet ")
		val request = Request(Code.GET)
		request.setPayload(v)
		
		val coapResp = coapClient.advanced(request)
		println(coapResp.responseText)
		
		var answer = coapResp.responseText
		//The "CoapResponse" message contains the response.
 		//println(Utils.prettyPrint(coapResp))
		GlobalScope.launch{
			actor.emit("answer", "answer($answer)")
		}
	}

	fun put(v: String) {
		val coapResp = coapClient.put(v, MediaTypeRegistry.TEXT_PLAIN)
		//The "CoapResponse" message contains the response.
		println("%%% ANSWER put $v:")
		println(coapResp.responseText)
		GlobalScope.launch{
			actor.emit("roomModelChanged", "modelChanged(fridge, ${coapResp.responseText})")
		}
	}

 	fun asynchGet() {
 		coapClient.get( AsynchListener );
	}
	
}