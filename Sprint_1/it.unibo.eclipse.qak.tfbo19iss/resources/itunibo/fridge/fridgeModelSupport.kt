package itunibo.fridge
import it.unibo.kactor.ActorBasic
import kotlinx.coroutines.launch

object fridgeModelSupport {
	
	fun answerRequest(actor: ActorBasic, source: String, foodcode: String) {
		actor.solve( "model( resource, fridge, state(STATE) )" )
		actor.solve( "contains($foodcode, ${actor.getCurSol("STATE")})" )
		actor.scope.launch {
			if(actor.solveOk()) {
				actor.emit("answer", "answer(yes)")
			} else {
				actor.emit("answer", "answer(no)")
			}
		}
	}
	
	fun updateFridgeModel( actor: ActorBasic, content: String ){
		actor.solve(  "action( fridge, $content )" ) //change the fridge state model
		actor.solve(  "model( A, fridge, STATE )" )
		actor.scope.launch{
			//sent to notify to the RBR that the change in the model has been performed
			actor.emit( "roomModelChanged" , "modelChanged(fridge, $content)" )
		}
	}
}