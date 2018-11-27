package com.ids;


import com.ids.param.ParamConfig;
import com.ids.shell.RunShell;
import org.junit.Test;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class TestShell {
    @Test
    public void testExec() {
        try {
            Runtime.getRuntime().exec("notepad.exe 1.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProcessBuilder() {
        try {
            String[] cmds = new String[]{
                    "cmd",
                    "dir"
            };
            Process process = new ProcessBuilder(cmds).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRunShell123() {
        RunShell.setEncode("gb2312");
        String[] cmds = new String[] {"cmd", "/c", "dir /?"};
        System.out.println(RunShell.run(cmds));

        System.out.println("=========================");
        System.out.println(RunShell.run("cmd /c dir"));
    }

    @Test
    public void testCmds() {
        String[] args;
        args = new String[] {"gb2312", "cmd", "/c", "dir", "/?"};
        //args = new String[] {"gb2312", "cmd", "/c", "help", "more"};

        int len = args.length;

        String encode = args[0];
        RunShell.setEncode(encode);

        List<String> lstCmds = new LinkedList<>();
        boolean isCmd = false;
        String cmd = "";
        for (int i=1; i<len; i++) {
            String curStr = args[i];
            if (curStr.equals("-c") || curStr.equals("/c")) {
                isCmd = true;
                lstCmds.add(curStr);
            } else {
                if (!isCmd) {
                    lstCmds.add(curStr);
                } else {
                    cmd += " " + curStr;
                }
            }
        }
        int i=0;
        String[] cmds = null;
        if (isCmd) {
            cmds = new String[lstCmds.size()+1];
        } else {
            cmds = new String[lstCmds.size()];
        }

        for (i=0; i<lstCmds.size(); i++) {
            cmds[i] = lstCmds.get(i);
        }
        if (isCmd) {
            cmds[i] = cmd;
        }
//        String[] cmds = new String[3];
//        cmds[0] = "sh";
//        cmds[1] = "-c";
//        cmds[2] = "systemctl status rsyslog";

        for (i=0; i<cmds.length; i++) {
            System.out.println(cmds[i]);
        }
        System.out.println(RunShell.run(cmds));
    }

    @Test
    public void testProcessBuilder2() {
        try {   // java javac 等调用不执行，原因未知
            String[] cmds = new String[]{
                    "cmd",
                    "/c",
                    "java -version"
            };
            Process process = new ProcessBuilder(cmds).start();

            InputStream ins = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "gb2312"));
            String line;
            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            ins.close();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testProcessInput() {
        try {
            String cmd = "cmd /c start java -jar E:\\idea\\git\\IdsConfig\\out\\artifacts\\IdsConfig_jar\\IdsConfig.jar";
            String[] cmds = {"cmd", "/c", "java -jar E:\\idea\\git\\IdsConfig\\out\\artifacts\\IdsConfig_jar\\IdsConfig.jar"};
            Process process = Runtime.getRuntime().exec(cmds);
            try {
                InputStream ins = process.getErrorStream();
                OutputStream outs = process.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ins, "gb2312"));
                String line;
                while((line = reader.readLine()) != null) {
                    //String rd = new String(line.getBytes("gb2312"), "utf-8");
                    System.out.println(line);
                }
                ins.close();
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test123() {
        try {
            String cmd;
            //cmd = "ping 127.0.0.1";
            cmd = "cmd /c java -version";
            Process p = Runtime.getRuntime().exec(cmd);
            InputStream is = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "gb2312"));
            String line;
            while((line = reader.readLine())!= null){
                System.out.println(line);
            }
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            is.close();
            reader.close();
            p.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1234() {
        try {
            String cmd;
            //cmd = "ping 127.0.0.1";
            cmd = "net user";
            Process p = Runtime.getRuntime().exec(new String[]{"javac", "E:\\idea\\git\\IdsConfig\\src\\test\\java\\com\\ids\\TestShell.java"},null,null);
            InputStream is = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "gb2312"));
            String line;
            while((line = reader.readLine())!= null){
                System.out.println(line);
            }
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            is.close();
            reader.close();
            p.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
