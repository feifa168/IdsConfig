#!/bin/bash
if [ ! -n "$1" ] ;then
    echo "please input real network card interface, for example eth0 !"
else
    export LD_LIBRARY_PATH=.:../jre/bin:$LD_LIBRARY_PATH
    iface=$1
    echo $iface
    java -agentlib:NativeDecrypt=dec_install.xml -jar install.jar -interface $iface
fi
