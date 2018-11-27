package com.ids.config;

import com.ids.param.ParamConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyslogConfig {
    public static String errMsg = null;
    private static ParamConfig.SyslogServer[] syslogServers;

    public static void setSyslogServers(ParamConfig.SyslogServer[] servers) { syslogServers = servers; }

    public static boolean modifySyslogConfigFile(String syslogconf) {
        RandomAccessFile raf = null;
        FileChannel fc  = null;
        boolean isok = false;

        if ((syslogServers == null) || (syslogServers.length == 0)) {
            errMsg = "there is no syslog server config to modify";
            return false;
        }

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

            ByteBuffer buf = ByteBuffer.allocate(len+syslogServers[0].padLen);
            int rdLen = 0;
            try {
                rdLen = fc.read(buf);
                buf.flip();
            } catch (IOException e) {
                //e.printStackTrace();
                errMsg = e.getMessage();
                break;
            }

            for (ParamConfig.SyslogServer server : syslogServers) {
                if (!matchContext(new String(buf.array()), server)) {
                    int curpostion = buf.limit();
                    if (addContext(buf, curpostion, server)) {
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

    private static boolean matchContext(String context, ParamConfig.SyslogServer server) {
        Matcher m = Pattern.compile(server.regex).matcher(context);

        String mark;
        String curfacility;
        String curlevel;
        String curudp;
        String curtcp;
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
            curtcp     = m.group("tcp");
            curip      = m.group("ip");
            curport    = m.group("port");
            if (server.facility.equals(curfacility)
                    && (server.level.equals(curlevel))
                    && (server.ip.equals(curip))
                    && (server.port.equals(curport))
                    ) {
                if ( (server.proto.equals("udp") && server.udp.equals(curudp))
                        || (server.proto.equals("tcp") && server.tcp.equals(curtcp)) ) {
                    isfind = true;
                    break;
                }
            }
        }
        return isfind;
    }

    private static boolean addContext(ByteBuffer buf, int curpostion, ParamConfig.SyslogServer server) {
        String proto = "@";
        if (server.proto.equals("udp")) {
            proto = server.udp;
        } else if (server.proto.equals("tcp")) {
            proto = server.tcp;
        } else {
            return false;
        }
        StringBuilder udpconf = new StringBuilder(server.padLen);
        udpconf.append("\n").append(server.facility).append(".").append(server.level).append(" ").append(proto).append(server.ip).append(":").append(server.port);
        buf.position(curpostion);
        buf.limit(curpostion+udpconf.length());
        buf.put(udpconf.toString().getBytes());
        return true;
    }
}
