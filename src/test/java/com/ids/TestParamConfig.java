package com.ids;

import com.ids.config.SoPathConfig;
import com.ids.config.SyslogConfig;
import com.ids.copy.commonio.CopyFile;
import com.ids.param.ParamConfig;
import com.ids.shell.RunShell;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestParamConfig {
    public String rTrim(String s) {
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

    @Test
    public void testRTrim() {
        String s1 = new String(" \nhello tom \t\n\t\r\n");
        String s2 = rTrim(s1);
        System.out.println(s2);
    }

    @Test
    public void testMatchs() {
        Matcher m = Pattern.compile("(\\n)?(tom|tom2)(\\r\\n|\\n|$)").matcher("hello\r\ntom4\n");
        while (m.find()) {
            System.out.println("un find");
        }
    }

    @Test
    public void test12334() {
        if (ParamConfig.parse("runwindow.xml")) {
            if ( (null != ParamConfig.syslogFile) && (null != ParamConfig.logServer)) {
                SyslogConfig.setSyslogServers(ParamConfig.logServer);
                if (SyslogConfig.modifySyslogConfigFile(ParamConfig.syslogFile)) {
                    System.out.println("parse " + ParamConfig.syslogFile + " ok");
                } else {
                    System.out.println(SyslogConfig.errMsg);
                }
            }
            ParamConfig.netIfaces[0].iface = "iface1";

            CopyFile cf = null;
            String srcDir = null;
            String destDir = null;
            // copy file
            if ( (null != ParamConfig.srcDir) && (null != ParamConfig.dstDir) ) {
                cf = new CopyFile();
                srcDir = ParamConfig.srcDir;
                destDir = ParamConfig.dstDir;
                if(cf.copyDirectory(srcDir, destDir)) {
                    System.out.println("copydirectory from "+srcDir+" to "+destDir+" ok");
                } else {
                    System.out.println("copydirectory from "+srcDir+" to "+destDir+" fail, error is " + cf.getErrMsg());
                }
            }

            if ( (null != ParamConfig.lib64SrcDir) && (null != ParamConfig.lib64DstDir) ) {
                srcDir = ParamConfig.lib64SrcDir;
                destDir = ParamConfig.lib64DstDir;
                if(cf.copyDirectory(srcDir, destDir)) {
                    System.out.println("copydirectory from "+srcDir+" to "+destDir+" ok");
                } else {
                    System.out.println("copydirectory from "+srcDir+" to "+destDir+" fail, error is " + cf.getErrMsg());
                }
            }

            // 修改ld.so.conf
            if ( (null != ParamConfig.ldSoFile) && (null != ParamConfig.soLoadPaths) ) {
                SoPathConfig.setLdSoFile(ParamConfig.ldSoFile);
                SoPathConfig.setSoLoadPaths(ParamConfig.soLoadPaths);
                if (SoPathConfig.modifyLdSoConfigFile()) {
                    System.out.println("modify "+ParamConfig.ldSoFile + " ok");
                } else {
                    System.out.println("modify "+ParamConfig.ldSoFile + " fail, error is "+SoPathConfig.errMsg);
                }
            }

            // 执行shell命令
            for(ParamConfig.ShellCommand command : ParamConfig.commands) {
                RunShell.setEncode(ParamConfig.encode);
                System.out.println(RunShell.run(command.params));
            }

            // 创建执行脚本
            if (ParamConfig.buildIdsRunShell()) {
                for (ParamConfig.NetInterface iface : ParamConfig.netIfaces) {
                    System.out.println("interface " + iface.iface + ", command " + iface.command);
                }
            }
        } else {
            System.out.println(ParamConfig.errMsg);
        }
    }
}
