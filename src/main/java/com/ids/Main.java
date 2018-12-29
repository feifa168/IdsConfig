package com.ids;

import com.ids.config.SoPathConfig;
import com.ids.config.SyslogConfig;
import com.ids.copy.commonio.CopyFile;
import com.ids.param.ParamConfig;
import com.ids.shell.RunShell;

import java.util.HashMap;
import java.util.Map;

public class Main {
    // 获取参数
    static Map<String, String> getArgMap(String[] args) {
        Map<String, String> map = new HashMap<>();
        String key = null, val = null;
        for (String tmp : args) {
            if (tmp.startsWith("-")) {
                if (key != null)
                    map.put(key, val);
                key = tmp;
                val = null;
            } else {
                val = tmp;
            }
        }
        if (key != null) {
            map.put(key, val);
        }

        return map;
    }

    public static void main(String[] args) {
        Map<String, String> mpArgs = getArgMap(args);

        if (ParamConfig.parse("run.xml")) {

            if ( (null != ParamConfig.syslogFile) && (null != ParamConfig.logServer)) {
                SyslogConfig.setSyslogServers(ParamConfig.logServer);
                if (SyslogConfig.modifySyslogConfigFile(ParamConfig.syslogFile)) {
                    System.out.println("parse " + ParamConfig.syslogFile + " ok");
                } else {
                    System.out.println(SyslogConfig.errMsg);
                }
            }

            // 参数-interface跟的是网卡
            String firstInterface = mpArgs.get("-interface");
            if ( (null != firstInterface) && (null != ParamConfig.netIfaces)) {
                ParamConfig.netIfaces[0].iface = firstInterface;
            }

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
