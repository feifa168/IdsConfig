package com.ids.config;

import com.ids.param.ParamConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoPathConfig {
    private static String ldSoFile;
    private static String[] soLoadPaths;
    public static String errMsg = null;

    public static void setLdSoFile(String ldSoFile) {
        SoPathConfig.ldSoFile = ldSoFile;
    }

    public static void setSoLoadPaths(String[] soLoadPaths) {
        SoPathConfig.soLoadPaths = soLoadPaths;
    }

    public static boolean modifyLdSoConfigFile() {
        RandomAccessFile raf = null;
        FileChannel fc  = null;
        boolean isok = false;

        do {
            try {
                raf = new RandomAccessFile(ldSoFile, "rw");
                fc = raf.getChannel();
                int len = 0;
                try {
                    len = (int)fc.size();
                } catch (IOException e) {
                    //e.printStackTrace();
                    errMsg = e.getMessage();
                    break;
                }

                ByteBuffer buf = ByteBuffer.allocate(len+512);
                try {
                    fc.read(buf);
                    buf.flip();
                } catch (IOException e) {
                    //e.printStackTrace();
                    errMsg = e.getMessage();
                    break;
                }

                String context = new String(buf.array());
                for (String text : soLoadPaths) {
                    Matcher m = Pattern.compile(text+"\\b").matcher(context);
                    if (!m.find()) {
                        isok = true;

                        String putStr = null;
                        if ((text.length() > 0) && (text.indexOf(text.length()-1) != '\n')) {
                            putStr = "\n"+text;
                        } else {
                            putStr = text;
                        }

                        int curPos = (int)buf.limit();
                        buf.position(curPos);
                        buf.limit(buf.limit()+putStr.length());
                        buf.put(putStr.getBytes());

                        try {
                            buf.position(curPos);
                            fc.write(buf);
                        } catch (IOException e) {
                            //e.printStackTrace();
                            errMsg = e.getMessage();
                            break;
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                errMsg = e.getMessage();
                break;
            }
        } while(false);

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
