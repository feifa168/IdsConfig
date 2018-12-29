@echo off
bin/snort -c etc/snort/snort.lua -i iface1 -l /var/log/snort --plugin-path extra -k none
