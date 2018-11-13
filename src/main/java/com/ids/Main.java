package com.ids;

import com.ids.syslog.SyslogConfig;

public class Main {

    public static void showHelp() {
        System.out.println(
                "please input arguments\n" +
                "for example\n" +
                 "/etc/rsyslog.conf"
        );
    }
    public static void main(String[] args) {
        if (args.length == 0) {
            showHelp();
            return;
        }

        if (SyslogConfig.parse("rsyslog.xml")) {
            if (SyslogConfig.modifySyslogConfigFile(args[0])) {
                System.out.println("ok");
            } else {
                System.out.println(SyslogConfig.errMsg);
            }
        } else {
            System.out.println(SyslogConfig.errMsg);
        }
    }
}
