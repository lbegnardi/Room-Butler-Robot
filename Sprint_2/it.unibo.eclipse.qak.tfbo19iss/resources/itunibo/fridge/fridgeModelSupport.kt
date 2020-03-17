package itunibo.fridge
import it.unibo.kactor.ActorBasic
import kotlinx.coroutines.launch

object fridgeModelSupport{
	
	fun answerRequest(actor: ActorBasic, source: String, foodcode: String) {
		actor.solve( "model( resource, fridge, state(STATE) )" )
		actor.solve( "contains($foodcode, ${actor.getCurSol("STATE")})" )
		actor.scope.launch {
			if(actor.solveOk()) {
				if(source == "roombutlerrobot") {
					actor.emit("answer", "answer(yes)")
				} else {
					actor.emit("answer", "answer(content(yes))")
				}
			} else {
				if(source == "roombutlerrobot") {
					actor.emit("answer", "answer(no)")
				} else {
					actor.emit("answer", "answer(content(no))")
				}
			}
		}
	}
	
	fun exposeFridgeModel( actor: ActorBasic ){
		actor.solve(  "model( A, fridge, STATE )" )
		val FridgeState = actor.getCurSol("STATE")
		actor.scope.launch{
			actor.emit( "fridgeContent" , "content( fridge( $FridgeState ) )" )
		}
	}
	
	fun updateFridgeModel( actor: ActorBasic, content: String ){
		actor.solve(  "action( fridge, $content )" ) //change the fridge state model
		actor.solve(  "model( A, fridge, STATE )" )
		//val FridgeState = actor.getCurSol("STATE")
		actor.scope.launch{
			//sent to notify to the RBR that the change in the model has been performed
			actor.emit( "roomModelChanged" , "modelChanged(fridge, $content)" )  //for the RBR
		}
	}
}