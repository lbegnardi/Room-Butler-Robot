/* Generated by AN DISI Unibo */ 
package it.unibo.fridge

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Fridge ( name: String, scope: CoroutineScope ) : ActorBasicFsm( name, scope){
 	
	override fun getInitialState() : String{
		return "s0"
	}
		
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						solve("consult('sysRules.pl')","") //set resVar	
						solve("consult('fridgeModel.pl')","") //set resVar	
					}
					 transition( edgeName="goto",targetState="waitCmd", cond=doswitch() )
				}	 
				state("waitCmd") { //this:State
					action { //it:State
					}
					 transition(edgeName="t00",targetState="handleRequest",cond=whenDispatch("request"))
					transition(edgeName="t01",targetState="updateModel",cond=whenDispatch("modelUpdate"))
				}	 
				state("handleRequest") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("request(S,F)"), Term.createTerm("request(S,F)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								itunibo.fridge.fridgeModelSupport.answerRequest(myself ,payloadArg(0), payloadArg(1) )
						}
					}
					 transition( edgeName="goto",targetState="waitCmd", cond=doswitch() )
				}	 
				state("updateModel") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("modelUpdate(TARGET,VALUE)"), Term.createTerm("modelUpdate(fridge,V)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								itunibo.fridge.fridgeModelSupport.updateFridgeModel(myself ,payloadArg(1) )
						}
					}
					 transition( edgeName="goto",targetState="waitCmd", cond=doswitch() )
				}	 
			}
		}
}
