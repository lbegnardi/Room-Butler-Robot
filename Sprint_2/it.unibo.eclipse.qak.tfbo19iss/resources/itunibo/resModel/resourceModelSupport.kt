package itunibo.resModel

import it.unibo.kactor.ActorBasic
import kotlinx.coroutines.launch

object resourceModelSupport {
	fun updateRobotModel( actor: ActorBasic, content: String ){
 		actor.solve(  "action(robot, move($content) )" ) //change the robot state model
		actor.solve(  "model( A, robot, STATE )" )
		val RobotState = actor.getCurSol("STATE")
		actor.scope.launch{
 			actor.emit( "robotModelChanged" , "modelChanged(  robot,  $content)" )  //for the robotmind
			actor.emit( "modelContent" , "content( robot( $RobotState ) )" )
  		}
	}

	fun updateSonarRobotModel( actor: ActorBasic, content: String ){
 		actor.solve( "action( sonarRobot,  $content )" ) //change the robot state model
		actor.solve( "model( A, sonarRobot, STATE )" )
		val SonarState = actor.getCurSol("STATE")
		actor.scope.launch{
 			actor.emit( "modelContent" , "content( sonarRobot( $SonarState ) )" )
 		}	
	}
	
	fun updateRoomResourceModel( actor: ActorBasic, resource: String, content: String ){
 		actor.solve(  "action( $resource, $content )" ) //change the room state model
		actor.solve(  "model( A, $resource, STATE )" )
		val RoomResState = actor.getCurSol("STATE")
		actor.scope.launch{
			//sent to notify to the RBR that the change in the model has been performed
			actor.emit( "local_robotModelChanged" , "modelChanged(  robot,  $content)" )  //for the robotmind
			//the action is performed by the robot immediately. In case of real robots a callback system has to be implemented
 			actor.emit( "roomModelChanged" , "modelChanged(  $resource,  $content)" )  //for the rbr
			actor.emit( "modelContent" , "content($resource( $RoomResState ))" )
  		}
	}
	
	fun updateRoomMapModel( actor: ActorBasic, content: String ) {
		actor.solve(  "action( roommap, update('$content'))" )
		actor.scope.launch{
			actor.emit( "modelContent" , "content( roomMap( state( '$content' ) ) )" )
 		}
	}
	
	fun getRobotModel( actor: ActorBasic){
		actor.solve(  "model( A, robot, STATE )" )
		val RobotState = actor.getCurSol("STATE")
		actor.scope.launch{
			actor.emit( "modelContent" , "content( robot( $RobotState ) )" )
  		}
	}

	fun getSonarRobotModel( actor: ActorBasic ){
		actor.solve( "model( A, sonarRobot, STATE )" )
		val SonarState = actor.getCurSol("STATE")
		actor.scope.launch{
 			actor.emit( "modelContent" , "content( sonarRobot( $SonarState ) )" )
 		}	
	}
	
	fun consultRoomResourceModel( actor: ActorBasic, resource: String) {
		actor.solve(  "model( A, $resource, state(STATE) )" )
		val RoomResState = actor.getCurSol("STATE")
		actor.scope.launch{
			actor.emit( "modelState" , "modelState( $RoomResState )")
  		}
	}
	
	fun exposeRoomResourceModel( actor: ActorBasic, resource: String ){
		actor.solve(  "model( A, $resource, STATE )" )
		val RoomResState = actor.getCurSol("STATE")
		actor.scope.launch{
			actor.emit( "modelContent" , "content($resource( $RoomResState ) )" )
  		}
	}

	fun getRoomMapModel( actor: ActorBasic ) {
		actor.solve( "model(A, roommap, STATE)" )
		val RoomMapState = actor.getCurSol("STATE")
		actor.scope.launch{
			actor.emit( "modelContent" , "content( roomMap( $RoomMapState ) )" )
		}
	}
	
	fun sendWarning(actor: ActorBasic) {
		actor.scope.launch{
			actor.emit( "warning" , "content( warning )" )
 		}
	}
}