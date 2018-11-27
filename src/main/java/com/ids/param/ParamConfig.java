package com.ids.param;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.List;

public class ParamConfig {
//<run>
//    <params>
//        <!--"/etc/rsyslog.conf /home/my/srdDir /home/my/destDir /home/my/srdDir2 /home/my/destDir2"-->
//        <param type="syslog">/etc/rsyslog.conf>
//            <items>
//                <item>
//                    <regex><![CDATA[\n\s?(?<mark>\#+)?\s?(?<facility>\*|\w+)\.(?<level>\*|\w+)\s+((?<tcp>\@@)|(?<udp>\@))(?<ip>\d+\.\d+\.\d+\.\d+)\:(?<port>\d+)]]></regex>
//                    <!-- * | auth | authpriv | daemon | user | local0 | local1 | local2 | local3 | local4 | local5 | local6 | local7 -->
//                    <facility>*</facility>
//                    <!-- * | emerg | alert | crit | err | warning | notice | info | debug -->
//                    <level>*</level>
//                    <proto>@</proto>
//                    <ip>127.0.0.1</ip>
//                    <port>514</port>
//                </item>
//            </items>
//        </param>
//        <param type="so">/etc/ld.so.conf</param>
//        <param type="ids">
//            <item type="directory">
//                <src>ids</src>
//                <dst>/usr/local/ids</dst>
//            </item>
//            <item type="lib64">
//                <src>lib64</src>
//                <dst>/usr/local/lib64</dst>
//            </item>
//        </param>
//    </params>
//</run>
    public static class SyslogServer {
        public String regex      = "\\n\\s?(?<mark>\\#+)?(?<facility>\\*|\\w+)\\.(?<level>\\*|\\w+)\\s+((?<tcp>\\@@)|(?<udp>\\@))(?<ip>\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(?<port>\\d+)";
        public String facility   = "*";
        public String level      = "*";
        public String proto      ="udp";
        public String udp        = "@";
        public String tcp        = "@@";
        public String ip         = "";
        public String port       = "514";
        public final int padLen = 1024;
        public SyslogServer() {}
    }
    public static class ShellCommand {
        public String command = "";
    }

    public static String errMsg     = null;

    public static SyslogServer[] logServer;
    public static String syslogFile    = "/etc/rsyslog.conf";
    public static String ldSoFile      = "/etc/ld.so.conf";
    public static String[] soLoadPaths;
    public static String srcDir        = "ids";
    public static String dstDir        = "/usr/local/ids";
    public static String lib64SrcDir   = "lib64";
    public static String lib64DstDir   = "/usr/local/lib64";
    public static String encode        = "utf-8";
    public static ShellCommand[] commands;

    public static boolean parse(String runXml) {
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(new File(runXml));
            Node ndSyslog       = doc.selectSingleNode("/run/params/param[@type=\"syslog\"]/file");
            List<Node> ndServer = doc.selectNodes("/run/params/param[@type=\"syslog\"]/items/item");
            Node ndSo           = doc.selectSingleNode("/run/params/param[@type=\"so\"]");
            List<Node> ndSoLoadPaths = doc.selectNodes("/run/params/param[@type=\"so\"]/items/item");
            Node ndDirectory    = doc.selectSingleNode("/run/params/param[@type=\"ids\"]/items/item[@type=\"directory\"]");
            Node ndLib64        = doc.selectSingleNode("/run/params/param[@type=\"ids\"]/items/item[@type=\"lib64\"]");
            Node ndEncode       = doc.selectSingleNode("/run/params/param[@type=\"shell\"]/items");
            List<Node> ndCommands   = doc.selectNodes("/run/params/param[@type=\"shell\"]/items/item");

            syslogFile  = parseField(ndSyslog, syslogFile);
            ldSoFile    = parseNodeAttribute(ndSo, "file", ldSoFile);
            soLoadPaths = parseSoPaths(ndSoLoadPaths);
            logServer   = parseLogServer(ndServer);

            if (ndDirectory != null) {
                Node ndSrcDir = ndDirectory.selectSingleNode("src");
                Node ndDstDir = ndDirectory.selectSingleNode("dst");
                srcDir = parseField(ndSrcDir, srcDir);
                dstDir = parseField(ndDstDir, dstDir);
            }
            if (ndLib64 != null) {
                Node ndLib64SrcDir   = ndLib64.selectSingleNode("src");
                Node ndLib64DstDir   = ndLib64.selectSingleNode("dst");
                lib64SrcDir     = parseField(ndLib64SrcDir, lib64SrcDir);
                lib64DstDir     = parseField(ndLib64DstDir, lib64DstDir);
            }

            encode = parseNodeAttribute(ndEncode, "encode", encode);

            commands = parseShellCommand(ndCommands);

            return true;

        } catch (DocumentException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        }

        return false;
    }

    private static String parseField(Node nd, String defaultValue) {
        if (nd != null) {
            String s = nd.getText();
            if (!s.equals("")) {
                return s;
            }
        }
        return defaultValue;
    }

    private static SyslogServer[] parseLogServer(List<Node> items) {
        if (items != null) {
            if (items.size() == 0) {
                errMsg = "three is no syslog server config";
                return null;
            }

            logServer = new SyslogServer[items.size()];
            int i=0;
            for (Node item : items) {
                SyslogServer curServer = new SyslogServer();
                logServer[i++] = curServer;
                if (item != null) {
                    Node ndRegex    = item.selectSingleNode("regex");
                    Node ndFacility = item.selectSingleNode("facility");
                    Node ndLevel    = item.selectSingleNode("level");
                    Node ndProto    = item.selectSingleNode("proto");
                    Node ndIp       = item.selectSingleNode("ip");
                    Node ndPort     = item.selectSingleNode("port");

                    parseSyslogServer(ndRegex, curServer);
                    parseSyslogServer(ndFacility, curServer);
                    parseSyslogServer(ndLevel, curServer);
                    if (ndProto != null) {
                        String s = ndProto.getText();
                        if (s.equals("udp")) {
                            curServer.proto = s;
                            curServer.udp = "@";
                        } else if (s.equals("tcp")) {
                            curServer.proto = s;
                            curServer.tcp = "@@";
                        }
                    }
                    parseSyslogServer(ndIp, curServer);
                    parseSyslogServer(ndPort, curServer);
                }
            }
            return logServer;
        }

        errMsg = "three is no valid syslog server config";
        return null;
    }

    private static boolean parseSyslogServer(Node nd, SyslogServer server) {
        if (nd != null) {
            String s = nd.getText();
            if (!s.equals("")) {
                try {
                    Field fd = server.getClass().getDeclaredField(nd.getName());
                    fd.setAccessible(true);
                    try {
                        fd.set(server, s);
                        return true;
                    } catch (IllegalAccessException e) {
                        //e.printStackTrace();
                        errMsg = e.getMessage();
                    }
                } catch (NoSuchFieldException e) {
                    //e.printStackTrace();
                    errMsg = e.getMessage();
                }
            }
        } else {
            errMsg = "three is no valid syslog server node config";
        }

        return false;
    }

    private static String parseNodeAttribute(Node nd, String attrName, String defaultValue) {
        if (nd != null) {
            Attribute attr = ((Element)nd).attribute(attrName);
            if (attr != null) {
                String s = attr.getText();
                if (!s.equals("")) {
                    return s;
                }
            }
        }
        return defaultValue;
    }

    private static String[] parseSoPaths(List<Node> items) {
        if (items != null) {
            if (items.size() == 0) {
                errMsg = "three is no syslog server config";
                return null;
            }

            soLoadPaths = new String[items.size()];
            int i=0;
            for (Node item : items) {
                if (item != null) {
                    soLoadPaths[i++] = item.getText();
                }
            }
            return soLoadPaths;
        }

        errMsg = "three is no valid load so path config";
        return null;
    }

    private static ShellCommand[] parseShellCommand(List<Node> items) {
        if (items != null) {
            if (items.size() == 0) {
                errMsg = "three is no run shell config";
                return null;
            }

            commands = new ShellCommand[items.size()];
            int i=0;
            for (Node item : items) {
                ShellCommand curShell = new ShellCommand();
                commands[i++] = curShell;
                if (item != null) {
                    Node ndCommand  = item.selectSingleNode("command");
                    curShell.command = parseField(ndCommand, "");
                }
            }
            return commands;
        }

        errMsg = "three is no valid syslog server config";
        return null;
    }
}
