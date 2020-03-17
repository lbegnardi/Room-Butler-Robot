package itunibo.rbr

import it.unibo.kactor.ActorBasic

object roomButlerRobotSupport {
	fun getGoalCoordX( actor : ActorBasic, goal : String ) : String{
		actor.solve( "goal($goal, X, _)" )
		var x = actor.getCurSol("X")
		return "$x"
	}
	
	fun getGoalCoordY( actor : ActorBasic, goal : String ) : String{
		actor.solve( "goal($goal, _, Y)" )
		var y = actor.getCurSol("Y")
		return "$y"
	}
	
	fun changePrepSet( actor : ActorBasic, content : String ) {
		actor.solve( "replaceRule(preparation(_), preparation([$content]))" )
	}
}