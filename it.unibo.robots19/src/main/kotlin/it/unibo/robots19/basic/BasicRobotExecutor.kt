package it.unibo.robots19.basic

import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ApplMessage
import kotlinx.coroutines.CoroutineScope

class BasicRobotExecutor(name: String, scope: CoroutineScope): ActorBasic(name, scope) {
    val sink = Sink("sink", scope)

    init {
        //clientTcp.subscribe(sink)
        clientWenvTcpObj.initClientConn()
    }

    //val c = "tcpClient"

    override suspend fun actorBody(msg: ApplMessage) {
        println("BasicRobotExecutor | receives $msg")
        when(msg.msgId()) {
            RobotCmds.id ->
                when(msg.msgContent()) {
                    RobotCmds.forwardStr -> { //robotCmd(forward)
                        clientWenvTcpObj.sendMsg(RobotCmds.cmdMap.get(RobotCmds.forwardStr)!!)
                    }
                    RobotCmds.backwardStr -> { //robotCmd(forward)
                        clientWenvTcpObj.sendMsg(RobotCmds.cmdMap.get(RobotCmds.backwardStr)!!)
                    }
                    RobotCmds.stopStr -> { //robotCmd(forward)
                        clientWenvTcpObj.sendMsg(RobotCmds.cmdMap.get(RobotCmds.stopStr)!!)
                    }
                    RobotCmds.turnLeftStr -> { //robotCmd(forward)
                        clientWenvTcpObj.sendMsg(RobotCmds.cmdMap.get(RobotCmds.turnLeftStr)!!)
                    }
                    RobotCmds.turnRightStr -> { //robotCmd(forward)
                        clientWenvTcpObj.sendMsg(RobotCmds.cmdMap.get(RobotCmds.turnRightStr)!!)
                    }
                    else -> println("BasicRobotExecutor | msg content UNKNOWN")
                } //when msg.msgContent
            else -> println("BasicRobotExecutor | msg=$msg UNKNOWN")
        } // when msg.msgId
    }
}