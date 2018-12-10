@echo off

IF {%1}=={} goto nointerface
set iface=%1
echo %iface%
java -agentlib:libNativeDecrypt=idsConfig.xml -jar IdsConfigEnc.jar -interface %iface%
goto end

:nointerface
echo "please input real network card interface, for example eth0 !"

:end
