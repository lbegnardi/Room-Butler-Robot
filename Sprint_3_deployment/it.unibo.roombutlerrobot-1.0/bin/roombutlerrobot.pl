%====================================================================================
% roombutlerrobot description   
%====================================================================================
mqttBroker("localhost", "1883").
context(ctxrbr, "localhost",  "MQTT", "0" ).
context(ctxdummyformind, "otherresourcelocalhost",  "MQTT", "0" ).
 qactor( resourcemodel, ctxdummyformind, "external").
  qactor( roombutlerrobot, ctxrbr, "it.unibo.roombutlerrobot.Roombutlerrobot").
  qactor( planexecutor, ctxrbr, "it.unibo.planexecutor.Planexecutor").
