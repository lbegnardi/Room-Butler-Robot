package it.unibo.robots19.basic

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.*
import java.net.Socket

object clientWenvTcpObj {
    private var hostName = "localhost"
    private var port = 8999
    private var sep = ";"
    private var outToServer: PrintWriter? = null
    private var inFromServer: BufferedReader? = null

    fun initClientConn(hostNameStr: String = hostName, portStr: String = "$port") {
        hostName = hostNameStr
        port = Integer.parseInt(portStr)
        try {
            val clientSocket = Socket(hostName, port)
            println("clientWenvTcp | CONNECTION DONE")
            inFromServer = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            outToServer = PrintWriter(clientSocket.getOutputStream())
            startTheReader()
        } catch (e: Exception) {
            println("clientWenvTcp | ERROR $e")
        }
    }

    fun sendMsg(jsonString: String) {
        val jsonObject = JSONObject(jsonString)
        val msg = "$sep${jsonObject.toString()}$sep"
        outToServer?.println(msg)
        outToServer?.flush()
    }

    private fun startTheReader() {
        GlobalScope.launch {
            while(true) {
                try {
                    val inputStr = inFromServer?.readLine()
                    val jsonMsgStr =
                        inputStr!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                    //println("clientWenvTcp | inputStr=$jsonMsgStr")
                    val jsonObject = JSONObject(jsonMsgStr)
                    //println("type: ${jsonObject.getString("type")}")
                    when(jsonObject.getString("type")) {
                        "webpage-ready" -> println("webpage-ready")
                        "sonar-activated" -> {
                            //println("sonar-activated")
                            val jsonArg = jsonObject.getJSONObject("arg")
                            val sonarName = jsonArg.getString("sonarName")
                            val distance = jsonArg.getString("distance")
                            println("clientWenvTcp | sonarName=$sonarName distance=$distance")
                        }
                        "collision" -> {
                            //println("collision")
                            val jsonArg = jsonObject.getJSONObject("arg")
                            val objectName = jsonArg.getString("objectName")
                            println("clientWenvTcp | collision objectName=$objectName")
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    } //startTheReader
}

