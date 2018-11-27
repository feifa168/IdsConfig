package com.ids.shell;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RunShell {
    private static String encode = "utf-8";
    public static void setEncode(String code) { encode = code; }

    public static String run(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            InputStream ins = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins, encode));
            StringBuilder sb = new StringBuilder(128);
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            ins.close();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
