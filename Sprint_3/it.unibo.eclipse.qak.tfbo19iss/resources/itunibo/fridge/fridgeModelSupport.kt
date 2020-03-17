package itunibo.fridge
import it.unibo.kactor.ActorBasic
import kotlinx.coroutines.launch
import itunibo.coap.fridgeResourceCoap

object fridgeModelSupport{
	lateinit var resourcecoap : fridgeResourceCoap
	
	fun setCoapResource( rescoap : fridgeResourceCoap ) {
		resourcecoap = rescoap
	}
	
	fun answerRequest(actor: ActorBasic, source: String, foodcode: String) {
		actor.solve( "model( resource, fridge, state(STATE) )" )
		actor.solve( "contains($foodcode, ${actor.getCurSol("STATE")})" )
		actor.scope.launch {
			if(actor.solveOk()) {
				if(source == "roombutlerrobot") {
					actor.emit("answer", "answer(yes)")
				} else {
					resourcecoap.updateAnswer( "yes" )
				}
			} else {
				if(source == "roombutlerrobot") {
					actor.emit("answer", "answer(no)")
				} else {
					resourcecoap.updateAnswer( "no" )
				}
			}
		}
	}
	
	fun exposeFridgeModel( actor: ActorBasic ){
		actor.solve(  "model( A, fridge, STATE )" )
		val FridgeState = actor.getCurSol("STATE")
		actor.scope.launch {
			resourcecoap.updateState( "fridge($FridgeState)" )
		}
	}
	
	fun updateFridgeModel( actor: ActorBasic, content: String ){
		actor.solve(  "action( fridge, $content )" ) //change the robot state model
		actor.solve(  "model( A, fridge, STATE )" )
		val FridgeState = actor.getCurSol("STATE")
		actor.scope.launch {
			//sent to notify to the RBR that the change in the model has been performed
			resourcecoap.updateState( "fridge($FridgeState)" )
		}
	}
}