@echo off
bin/snort -c etc/snort/snort.lua -i eth2 -l /var/log/snort --plugin-path extra -k none
