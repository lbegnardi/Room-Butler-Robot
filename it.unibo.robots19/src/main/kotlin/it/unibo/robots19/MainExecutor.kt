package it.unibo.robots19

import it.unibo.kactor.MsgUtil
import it.unibo.kactor.QakContext
import it.unibo.robots19.basic.RobotCmds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("START")
    QakContext.createContexts(
        "localhost", this,
        "executorDescr.pl",
        "sysRules.pl"
    )

    val robot = QakContext.getActor("robot")
    MsgUtil.sendMsg("main", RobotCmds.id, RobotCmds.turnLeftStr, robot!!)
    delay(1000)
    MsgUtil.sendMsg(RobotCmds.id, RobotCmds.turnRightStr, robot!!)
    delay(1000)
    MsgUtil.sendMsg(RobotCmds.id, RobotCmds.forwardStr, robot!!)
    delay(1000)
    MsgUtil.sendMsg(RobotCmds.id, RobotCmds.backwardStr, robot!!)
    delay(1000)
    MsgUtil.sendMsg(RobotCmds.id, RobotCmds.stopStr, robot!!)
    println("END")
}