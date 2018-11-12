package com.ids;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestSyslogConfig {
    @Test
    public void testModifySyslogConfig() {
        RandomAccessFile raf = null;
        String fileName = "rsyslog.conf";
        try {
            raf = new RandomAccessFile(fileName, "rw");
            FileChannel fc = raf.getChannel();

            try {
                int len = (int)fc.size();
                if (len < 0) {
                    System.out.println("无效文件");
                    return;
                }

                ByteBuffer buf = ByteBuffer.allocate(len+256);
                int rdLen = fc.read(buf);
                System.out.println(len + ", " + rdLen);

                // 检查是否已经存在该配置
                int curpos = buf.position();
                String pad = "                                                                            ";
                buf.put(pad.getBytes());
                buf.flip();
                String conf = new String(buf.array());
//<syslog>
//    <parse>
//        <item type="udp">
//            <regex><![CDATA[\s*[^#](\*|\w+)\.(\*|\w+)\s+\@(?<ip>\d+\.\d+\.\d+\.\d+)\:(?<port>\d+)]]></regex>
                Matcher m = Pattern.compile("\\s?(?<mark>\\#+)?\\s?(?<facility>\\*|\\w+)\\.(?<level>\\*|\\w+)\\s+((?<tcp>\\@@)|(?<udp>\\@))(?<ip>\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(?<port>\\d+)")
                        .matcher(conf);
                boolean isfind = false;
                // append your server
                String defaultFacility = new String("snort");
                String defaultLevel = new String("alert");
                String defaultUdp = new String("@");
                String defaultIp = new String("127.0.0.1");
                String defaultPort = new String("514");

                String mark;
                String facility = "snort";
                String level = "alert";
                String udp = "";
                String ip = "";
                String port = "";
                while (m.find()) {
                    if (m.group("mark") != null) {
                        break;
                    }
                    facility = m.group("facility");
                    level   = m.group("level");
                    udp     = m.group("udp");
                    ip      = m.group("ip");
                    port    = m.group("port");
                    if (defaultFacility.equals(facility)
                            && (defaultLevel.equals(level))
                            && (defaultUdp.equals(udp))
                            && (defaultIp.equals(ip))
                            && (defaultPort.equals(port))
                    ) {
                        isfind = true;
                        break;
                    }
                }

                if (!isfind) {
                    buf.position(curpos);
                    StringBuilder udpconf = new StringBuilder(128);
                    udpconf.append("\n").append(defaultFacility).append(".").append(defaultLevel).append(" ")
                            .append(defaultUdp).append(defaultIp).append(":").append(defaultPort);
                    buf.put(udpconf.toString().getBytes());

                    long pos = fc.position();
                    buf.flip();
                    int iwrite = fc.write(ByteBuffer.wrap(udpconf.toString().getBytes()));//(buf, curpos);
                    pos = fc.position();
                    int sf = 78;
                }

//                buf.flip();
//                Charset set = Charset.forName("utf-8");
//                CharBuffer cbuf = set.decode(buf);
//                String data = cbuf.toString();
//                System.out.println(data);

                buf.flip();
                String s = new String(buf.array(), "utf-8");
                System.out.println(s);

                fc.close();
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
