#!/bin/bash
if [ ! -n "$1" ] ;then
    echo "please input real network card interface, for example eth0 !"
else
    export export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.
    iface=$1
    echo $iface
    java -agentlib:NativeDecrypt=idsConfig.xml -jar IdsConfigEnc.jar -interface $iface
fi
