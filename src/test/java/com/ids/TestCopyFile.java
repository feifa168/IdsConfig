package com.ids;

import com.ids.copy.CopyFile;
import org.junit.Test;

public class TestCopyFile {
    @Test
    public void testChannel() {
        CopyFile cf = new CopyFile();
        if (cf.copyByChannel("1.exe", "11.exe", true)) {
            System.out.println("ok");
        } else {
            System.out.println(cf.getErrMsg());
        }

        if (cf.copyByChannel("2.exe", "22.exe", false)) {
            System.out.println("ok");
        } else {
            System.out.println(cf.getErrMsg());
        }
    }

    @Test
    public void testAsync() {
        CopyFile cf = new CopyFile();
        if (cf.copyByAsync("1.exe", "111.exe", false)) {
            System.out.println("ok");
        } else {
            System.out.println(cf.getErrMsg());
        }

        if (cf.copyByAsync("2.exe", "222.exe", true)) {
            System.out.println("ok");
        } else {
            System.out.println(cf.getErrMsg());
        }
    }
}
