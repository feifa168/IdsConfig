package com.ids;

import org.junit.jupiter.api.Test;

import java.io.*;

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
