/* Generated by AN DISI Unibo */ 
package it.unibo.ctxDummyForMind
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	QakContext.createContexts(
	        "resourcelocalhost", this, "roombutlerrobot.pl", "sysRules.pl"
	)
}

