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

                byte[] bts = new byte[buf.limit()];
                buf.get(bts);
                String context = rTrim(new String(bts));

                for (String text : soLoadPaths) {
                    Matcher m = Pattern.compile(text+"(\\r\\n|\\n|\\s|$)").matcher(context);

                    if (m.find()) {
                        isok = true;
                        break;
                    } else {
                        String putStr = null;
                        if ((text.length() > 0) && (text.indexOf(text.length()-1) != '\n')) {
                            putStr = "\n"+text;
                        } else {
                            putStr = text;
                        }

                        int offset = context.length() - buf.limit();

                        int curPos = (int)(buf.limit() + offset);
                        buf.position(curPos);
                        buf.limit(curPos+putStr.length());
                        buf.put(putStr.getBytes());

                        try {
                            isok = true;
                            buf.position(curPos);
                            fc.write(buf, curPos);
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

    private static String rTrim(String s) {
        int offset = s.length();
        while (offset > 0) {
            char c = s.charAt(offset-1);
            if ( c=='\n' || c=='\r' || c==' ' || c=='\t') {
                offset--;
            } else {
                break;
            }
        }

        if (offset == 0) {
            return "";
        }

        return s.substring(0, offset);
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
