package itunibo.coap
import org.eclipse.californium.core.coap.CoAP.ResponseCode.BAD_REQUEST
import org.eclipse.californium.core.coap.CoAP.ResponseCode.CHANGED
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.coap.CoAP.ResponseCode
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.eclipse.californium.core.server.resources.CoapExchange
import it.unibo.kactor.ActorBasic
import it.unibo.kactor.MsgUtil
import org.eclipse.californium.core.CoapServer
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.GlobalScope
import org.eclipse.californium.core.coap.CoAP.Type
import itunibo.fridge.fridgeModelSupport

class fridgeResourceCoap (name : String ) : CoapResource(name) {
	
	companion object {
		lateinit var actor : ActorBasic
		var curmodelval = "unknown"
		var curanswer = "unknown"
		lateinit var resourceCoap : fridgeResourceCoap
		
		fun create( a: ActorBasic, name: String  ){
			actor = a
			val server   = CoapServer(5683);		//COAP SERVER
			resourceCoap = fridgeResourceCoap( name )
			server.add( resourceCoap );
			println("--------------------------------------------------")
			println("Coap Server started");	
			println("--------------------------------------------------")
			server.start();
			fridgeModelSupport.setCoapResource(resourceCoap)  //Injects a reference
		}
		
		fun getModel() : String {
			return curmodelval
		}
	
		fun getAnswer() : String {
			return curanswer
		}	
	}
	
	init { 
		println("--------------------------------------------------")
		println("fridgeResourceCoap init")
		println("--------------------------------------------------")
		setObservable(true) 				// enable observing	!!!!!!!!!!!!!!
		setObserveType(Type.CON)			// configure the notification type to CONs
		//getAttributes().setObservable();	// mark observable in the Link-Format			
	}
	
	fun updateAnswer( answeritem : String ){
		curanswer = answeritem
		//println("%%%%%%%%%%%%%%%% updateState from $curState to $curmodelval" )
		changed()	// notify all CoAp observers		
        	/*
        	 * Notifies all CoAP clients that have established an observe relation with
        	 * this resource that the state has changed by reprocessing their original
        	 * request that has established the relation. The notification is done by
        	 * the executor of this resource or on the executor of its parent or
        	 * transitively ancestor. If no ancestor defines its own executor, the
        	 * thread that has called this method performs the notification.
        	 */
	}
	
	fun updateState( modelitem : String ){
		curmodelval = modelitem
		//println("%%%%%%%%%%%%%%%% updateState from $curState to $curmodelval" )
		changed()	// notify all CoAp observers		
        	/*
        	 * Notifies all CoAP clients that have established an observe relation with
        	 * this resource that the state has changed by reprocessing their original
        	 * request that has established the relation. The notification is done by
        	 * the executor of this resource or on the executor of its parent or
        	 * transitively ancestor. If no ancestor defines its own executor, the
        	 * thread that has called this method performs the notification.
        	 */
	}
	 
	override fun handleGET(exchange: CoapExchange?) {
		try {
			val value = exchange!!.getRequestText()		//new String(payload, "UTF-8");
			println(value)		
			GlobalScope.launch{
				if(value == "") {
					MsgUtil.sendMsg( "expose", "expose", actor )
					delay(100)
					exchange.respond(ResponseCode.CONTENT, curmodelval, MediaTypeRegistry.TEXT_PLAIN)
				}
				else {
					MsgUtil.sendMsg( "request", "request(maitre, $value)", actor)	
					delay(100)
					exchange.respond(ResponseCode.CONTENT, curanswer, MediaTypeRegistry.TEXT_PLAIN)
				}
			}
 		} catch (e: Exception) {
			exchange!!.respond(BAD_REQUEST, "Invalid String")
		}
		
	}

	override fun handlePOST(exchange: CoapExchange?) {
 		//println("%%%%%%%%%%%%%%%% handlePOST  "  )
		handlePUT( exchange )
	}
	
	override fun handlePUT(exchange: CoapExchange?) {
		try {
			val value = exchange!!.getRequestText()//new String(payload, "UTF-8");'
			//val curState = curmodelval
			GlobalScope.launch {
				MsgUtil.sendMsg( "modelUpdate", "modelUpdate(fridge, $value )", actor )
				delay(100)  //give the time to change the model
				//updateState()
 				exchange.respond(CHANGED, value)
			}
 		} catch (e: Exception) {
			exchange!!.respond(BAD_REQUEST, "Invalid String")
		}
	}
}