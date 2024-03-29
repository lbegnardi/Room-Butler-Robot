/* Generated by AN DISI Unibo */ 
package it.unibo.robotmind

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Robotmind ( name: String, scope: CoroutineScope ) : ActorBasicFsm( name, scope){
 	
	override fun getInitialState() : String{
		return "s0"
	}
		
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		var goingForward = false
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						println("ROBOT MIND STARTED")
					}
					 transition( edgeName="goto",targetState="waitCmd", cond=doswitch() )
				}	 
				state("waitCmd") { //this:State
					action { //it:State
					}
					 transition(edgeName="t06",targetState="handleEnvCond",cond=whenEvent("envCond"))
					transition(edgeName="t07",targetState="handleSonarRobot",cond=whenEvent("sonarRobot"))
					transition(edgeName="t08",targetState="handleModelChanged",cond=whenEvent("local_robotModelChanged"))
				}	 
				state("handleEnvCond") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("envCond(CONDTYPE)"), Term.createTerm("envCond(CMD)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								forward("robotCmd", "robotCmd(h)" ,"basicrobot" ) 
								forward("modelUpdate", "modelUpdate(robot,h)" ,"resourcemodel" ) 
						}
					}
					 transition( edgeName="goto",targetState="waitCmd", cond=doswitch() )
				}	 
				state("handleModelChanged") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("modelChanged(TARGET,VALUE)"), Term.createTerm("modelChanged(robot,CMD)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								forward("robotCmd", "robotCmd(${payloadArg(1)})" ,"basicrobot" ) 
								goingForward= (payloadArg(1)=="w")
						}
					}
					 transition( edgeName="goto",targetState="waitCmd", cond=doswitch() )
				}	 
				state("handleSonarRobot") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("sonar(DISTANCE)"), Term.createTerm("sonar(D)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								forward("modelUpdate", "modelUpdate(sonarRobot,${payloadArg(0)})" ,"resourcemodel" ) 
						}
					}
					 transition( edgeName="goto",targetState="waitCmd", cond=doswitch() )
				}	 
				state("handeObstacle") { //this:State
					action { //it:State
						if(goingForward){ itunibo.robotMbot.globalTimer.stopTimer( "mind"  )
						forward("robotCmd", "robotCmd(h)" ,"basicrobot" ) 
						forward("modelUpdate", "modelUpdate(robot,h)" ,"resourcemodel" ) 
						println("HANDLE OBSTACLE !!!")
						 }
					}
					 transition( edgeName="goto",targetState="waitCmdAtObstacle", cond=doswitch() )
				}	 
				state("waitCmdAtObstacle") { //this:State
					action { //it:State
					}
					 transition(edgeName="t09",targetState="handleEnvCond",cond=whenEvent("envCond"))
					transition(edgeName="t010",targetState="handleModelChanged",cond=whenEvent("local_robotModelChanged"))
				}	 
			}
		}
}
