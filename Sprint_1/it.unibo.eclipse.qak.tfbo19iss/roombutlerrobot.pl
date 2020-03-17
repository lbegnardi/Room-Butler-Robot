%====================================================================================
% roombutlerrobot description   
%====================================================================================
mqttBroker("localhost", "1883").
context(ctxrbr, "localhost",  "MQTT", "0" ).
context(ctxdummyformind, "resourcelocalhost",  "MQTT", "0" ).
context(ctxdummyforfridge, "fridgehost",  "MQTT", "0" ).
 qactor( resourcemodel, ctxdummyformind, "external").
  qactor( fridge, ctxdummyforfridge, "external").
  qactor( roombutlerrobot, ctxrbr, "it.unibo.roombutlerrobot.Roombutlerrobot").
  qactor( planexecutor, ctxrbr, "it.unibo.planexecutor.Planexecutor").
  qactor( emulatedmaitre, ctxrbr, "it.unibo.emulatedmaitre.Emulatedmaitre").
