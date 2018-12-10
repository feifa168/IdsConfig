package com.ids.param;

import com.ids.shell.RunShell;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidParameterException;
import java.util.List;

public class ParamConfig {
    // <?xml version="1.0" encoding="UTF-8"?>
//
// <run>
//     <params>
//         <!--"/etc/rsyslog.conf /home/my/srdDir /home/my/destDir /home/my/srdDir2 /home/my/destDir2"-->
//         <param type="syslog">
//             <file>/etc/rsyslog.conf</file>
//             <items>
//                 <item>
//                     <regex><![CDATA[\n\s?(?<mark>\#+)?\s?(?<facility>\*|\w+)\.(?<level>\*|\w+)\s+((?<tcp>\@@)|(?<udp>\@))(?<ip>\d+\.\d+\.\d+\.\d+)\:(?<port>\d+)]]></regex>
//                     <!-- * | auth | authpriv | daemon | user | local0 | local1 | local2 | local3 | local4 | local5 | local6 | local7 -->
//                     <facility>snort</facility>
//                     <!-- * | emerg | alert | crit | err | warning | notice | info | debug -->
//                     <level>alert</level>
//                     <proto>udp</proto>
//                     <ip>172.16.39.21</ip>
//                     <port>514</port>
//                 </item>
//             </items>
//         </param>
//         <param type="so" file="/etc/ld.so.conf">
//             <items>
//                 <item>/usr/local/lib64</item>
//             </items>
//         </param>
//         <param type="ids">
//             <items>
//                 <item type="directory">
//                     <src>../ids</src>
//                     <dst>/usr/local/ids</dst>
//                 </item>
//                 <item type="lib64">
//                     <src>../lib64</src>
//                     <dst>/usr/local/lib64</dst>
//                 </item>
//             </items>
//         </param>
//         <param type="interface">
//             <items>
//                 <item>
//                     <interface>${interface}</interface>
//                     <command>bin/snort -c etc/snort/snort.lua -i ${interface} -l /var/log/snort --plugin-path extra -k none</command>
//                     <dst>/etc/rc.d/init.d</dst>
//                     <shells>
//                         <shell>
//                             <params>
//                                 <param>sh</param>
//                                 <param>-c</param>
//                                 <param>vi ${file}</param>
//                             </params>
//                         </shell>
//                         <shell>
//                             <params>
//                                 <param>sh</param>
//                                 <param>-c</param>
//                                 <param>chmod +x ${file}</param>
//                             </params>
//                         </shell>
//                         <shell>
//                             <params>
//                                 <param>sh</param>
//                                 <param>-c</param>
//                                 <param>chkconfig --add ${file}</param>
//                             </params>
//                         </shell>
//                         <shell>
//                             <params>
//                                 <param>sh</param>
//                                 <param>-c</param>
//                                 <param>chkconfig ${file} on</param>
//                             </params>
//                         </shell>
//                     </shells>
//                 </item>
//             </items>
//         </param>
//         <param type="shell">
//             <items  encode="utf-8">
//                 <item>
//                     <params>
//                         <param>sh</param>
//                         <param>-c</param>
//                         <param>ldconfig</param>
//                     </params>
//                 </item>
//                 <item>
//                     <params>
//                         <param>sh</param>
//                         <param>-c</param>
//                         <param>systemctl restart rsyslog</param>
//                     </params>
//                 </item>
//                 <item>
//                     <params>
//                         <param>sh</param>
//                         <param>-c</param>
//                         <param>mkdir /var/log/snort</param>
//                     </params>
//                 </item>
//             </items>
//         </param>
//     </params>
// </run>
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
        public String[] params = null;
    }

    public static class NetInterface {
        public String iface;
        public String command;
        public String dst;
        ShellCommand[] shellCommands;
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
    public static NetInterface[] netIfaces;
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
            List<Node> ndInterfaces   = doc.selectNodes("/run/params/param[@type=\"interface\"]/items/item");
            Node ndEncode       = doc.selectSingleNode("/run/params/param[@type=\"shell\"]/items");
            List<Node> ndCommands   = doc.selectNodes("/run/params/param[@type=\"shell\"]/items/item");

            syslogFile  = parseField(ndSyslog, syslogFile);
            ldSoFile    = parseNodeAttribute(ndSo, "file", ldSoFile);
            soLoadPaths = parseFields(ndSoLoadPaths);
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

            netIfaces = parseNetInterface(ndInterfaces);
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

    private static String[] parseFields(List<Node> items) {
        if (items != null) {
            if (items.size() == 0) {
                errMsg = "three is no config";
                return null;
            }

            String[] fields = new String[items.size()];
            int i=0;
            for (Node item : items) {
                if (item != null) {
                    fields[i++] = item.getText();
                }
            }
            return fields;
        }

        errMsg = "three is no valid config";
        return null;
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

    private static NetInterface[] parseNetInterface(List<Node> items) {
        if (items != null) {
            if (items.size() == 0) {
                errMsg = "three is no net interface config";
                return null;
            }

            NetInterface[] ifaces = new NetInterface[items.size()];
            for (int i=0; i< items.size(); i++) {
                NetInterface iface = new NetInterface();
                ifaces[i] = iface;

                Node item = items.get(i);
                if (item != null) {
                    iface.iface = parseField(item.selectSingleNode("interface"), null);
                    iface.command = parseField(item.selectSingleNode("command"), null);
                    iface.dst = parseField(item.selectSingleNode("dst"), null);
                    iface.shellCommands = parseShellCommand(item.selectNodes("shells/shell"));
                }
            }
            return ifaces;
        }

        errMsg = "three is no valid net interface config";
        return null;
    }

    private static ShellCommand[] parseShellCommand(List<Node> items) {
        if (items != null) {
            if (items.size() == 0) {
                errMsg = "three is no run shell config";
                return null;
            }

            ShellCommand[] cmds = new ShellCommand[items.size()];
            for (int i=0; i< items.size(); i++) {
                ShellCommand curShell = new ShellCommand();
                cmds[i] = curShell;

                Node item = items.get(i);
                if (item != null) {
                    List<Node> ndParams  = item.selectNodes("params/param");
                    int sz = ndParams.size();
                    if (sz > 0) {
                        curShell.params = new String[sz];
                        for (int j=0; j<sz; j++) {
                            Node ndParam = ndParams.get(j);
                            curShell.params[j] = parseField(ndParam, "");
                        }
                    }
                }
            }
            return cmds;
        }

        errMsg = "three is no valid syslog server config";
        return null;
    }

    public static boolean buildIdsRunShell() {
        String osname = System.getProperty("os.name");
        if (osname == null) {
            osname = "Windows";
        }
        osname = osname.toLowerCase();

        boolean isok = false;
        for (NetInterface iface : netIfaces) {
            StringBuilder sb = new StringBuilder(128);
            String suffixName; // 后缀名
            if (osname.startsWith("linux")) {
                sb.append("#!/bin/bash\n")
                        .append("#chkconfig: 2345 80 90\n")
                        .append("#description:auto_run\n")
                        .append("export export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.\n");
                suffixName = ".sh";
            } else if (osname.startsWith("windows")){
                sb.append("@echo off\n");
                suffixName = ".bat";
            } else {
                continue;
            }

            isok = true;
            String idspath = null;
            if (dstDir.endsWith("\\") || dstDir.endsWith("/")) {
                idspath = dstDir.substring(0, dstDir.length()-1);
            } else {
                idspath = dstDir;
            }
            iface.command = iface.command.replace("${interface}", iface.iface)
                                            .replaceAll("\\$\\{idspath\\}", idspath);
            sb.append(iface.command).append("\n");
            String shellPath = iface.dst;
            if (!shellPath.endsWith("\\") && !shellPath.endsWith("/")) {
                shellPath += System.getProperty("file.separator");
            }
            String commandFile = shellPath + iface.iface + suffixName;
            FileChannel fc = null;
            try {
                fc = FileChannel.open(Paths.get(commandFile), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                fc.write(ByteBuffer.wrap(sb.toString().getBytes()));
                fc.close();

                for (int i=0; i<iface.shellCommands.length; i++) {
                    System.out.print("");
                    for (int j=0; j<iface.shellCommands[i].params.length; j++){
                        iface.shellCommands[i].params[j] = iface.shellCommands[i].params[j].replace("${file}", commandFile);
                        System.out.print(iface.shellCommands[i].params[j] + " ");
                    }
                    System.out.print(" return is ");
                    System.out.println(RunShell.run(iface.shellCommands[i].params));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!isok) {
            errMsg = "there is no interface";
        }
        return isok;
    }
}
