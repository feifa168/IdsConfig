package com.ids.syslog;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// <syslog>
//     <parse>
//         <item type="udp">
//             <regex><![CDATA[]]></regex>
//             <!-- * | auth | authpriv | daemon | user | local0 | local1 | local2 | local3 | local4 | local5 | local6 | local7 -->
//             <facility>*</facility>
//             <!-- * | emerg | alert | crit | err | warning | notice | info | debug -->
//             <level>*</level>
//             <proto>@</proto>
//             <ip>127.0.0.1</ip>
//             <port>514</port>
//         </item>
//     </parse>
// </syslog>
public class SyslogConfig {
    public static String regex      = "\\n\\s?(?<mark>\\#+)?(?<facility>\\*|\\w+)\\.(?<level>\\*|\\w+)\\s+((?<tcp>\\@@)|(?<udp>\\@))(?<ip>\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(?<port>\\d+)";
    public static String facility   = "*";
    public static String level      = "*";
    public static String udp        = "@";
    public static String tcp        = "@@";
    public static String ip         = "";
    public static String port       = "514";
    public static String errMsg     = null;
    private static final int padLen = 256;

    public static boolean parse(String xml) {
        SAXReader reader = new SAXReader();
        try {
            Document doc = reader.read(new File(xml));
            Node item = doc.selectSingleNode("/syslog/parse/item[@type=\"udp\"]");
            if (item != null) {
                Node ndRegex    = item.selectSingleNode("regex");
                Node ndFacility = item.selectSingleNode("facility");
                Node ndLevel    = item.selectSingleNode("level");
                Node ndUdp      = item.selectSingleNode("udp");
                Node ndIp       = item.selectSingleNode("ip");
                Node ndPort     = item.selectSingleNode("port");
                String s;
                if (ndRegex != null) {
                    s = ndRegex.getText();
                    if (!s.equals(""))
                        regex = s;
                }
                if (ndFacility != null) {
                    s = ndFacility.getText();
                    if (!s.equals(""))
                        facility = s;
                }
                if (ndLevel != null) {
                    s = ndLevel.getText();
                    if (!s.equals(""))
                        level = s;
                }
                if (ndUdp != null) {
                    s = ndUdp.getText();
                    if (!s.equals(""))
                        udp = s;
                }
                if (ndIp != null) {
                    s = ndIp.getText();
                    if (!s.equals(""))
                        ip = s;
                }
                if (ndPort != null) {
                    s = ndPort.getText();
                    if (!s.equals(""))
                        port = s;
                }
                if (ip.length() > 0)
                    return true;
            }
        } catch (DocumentException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        }
        return false;
    }

    public static boolean modifySyslogConfigFile(String syslogconf) {
        RandomAccessFile raf = null;
        FileChannel      fc  = null;
        boolean isok = false;

        do {
            try {
                raf = new RandomAccessFile(syslogconf, "rw");
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                errMsg = e.getMessage();
                break;
            }

            fc = raf.getChannel();

            int len = 0;
            try {
                len = (int)fc.size();
            } catch (IOException e) {
                //e.printStackTrace();
                errMsg = e.getMessage();
                break;
            }

            if (len < 0) {
                errMsg = syslogconf + " is empty";
                break;
            }

            ByteBuffer buf = ByteBuffer.allocate(len+padLen);
            int rdLen = 0;
            try {
                rdLen = fc.read(buf);
                buf.flip();
            } catch (IOException e) {
                //e.printStackTrace();
                errMsg = e.getMessage();
                break;
            }

            if (!matchContext(new String(buf.array()))) {
                int curpostion = buf.limit();
                if (addContext(buf, curpostion)) {
                    // 设置位置为写入之前的地方，因为追加已经到大文件末尾
                    buf.position(curpostion);
                    try {
                        fc.write(buf);
                        isok = true;
                    } catch (IOException e) {
                        //e.printStackTrace();
                        errMsg = e.getMessage();
                        break;
                    }
                }
            } else {
                isok = true;
            }
        }while(false);

        try {
            if (fc != null) { fc.close(); }
            if(raf != null) { raf.close(); }
        }catch (IOException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        }
        return isok;
    }

    public static boolean matchContext(String context) {
        Matcher m = Pattern.compile(regex).matcher(context);

        String mark;
        String curfacility;
        String curlevel;
        String curudp;
        String curip;
        String curport;
        boolean isfind = false;

        while (m.find()) {
            if (m.group("mark") != null) {
                continue;
            }
            curfacility = m.group("facility");
            curlevel   = m.group("level");
            curudp     = m.group("udp");
            curip      = m.group("ip");
            curport    = m.group("port");
            if (facility.equals(curfacility)
                    && (level.equals(curlevel))
                    && (udp.equals(curudp))
                    && (ip.equals(curip))
                    && (port.equals(curport))
                    ) {
                isfind = true;
                break;
            }
        }
        return isfind;
    }

    public static boolean addContext(ByteBuffer buf, int curpostion) {
        StringBuilder udpconf = new StringBuilder(padLen);
        udpconf.append("\n").append(facility).append(".").append(level).append(" ")
                .append(udp).append(ip).append(":").append(port);
        buf.position(curpostion);
        buf.limit(curpostion+udpconf.length());
        buf.put(udpconf.toString().getBytes());
        return true;
    }
}
