@echo off

IF {%1}=={} goto nointerface
set iface=%1
echo %iface%
set path=.;..\jre\bin;%path%
echo %path%
java -agentlib:libNativeDecrypt=dec_install.xml -jar install.jar -interface %iface%
goto end

:nointerface
echo "please input real network card interface, for example eth0 !"

:end
