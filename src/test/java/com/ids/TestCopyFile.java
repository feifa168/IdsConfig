package com.ids;

import com.ids.copy.CopyFile;
import org.junit.Test;

public class TestCopyFile {
    public static void testByChannel(String srcFile, String dstFile, boolean isOveride) {
        CopyFile cf = new CopyFile();
        long stm = System.currentTimeMillis();
        if (cf.copyByChannel(srcFile, dstFile, isOveride)) {
            System.out.println("ok");
        } else {
            System.out.println(cf.getErrMsg());
        }
        System.out.println(System.currentTimeMillis() - stm);
    }

    @Test
    public void testChannel() {
        //for (int i=0; i<10; i++)
            testByChannel("2.zip", "22.zip", true);
//        testByChannel("1.exe", "11.exe", true);
//        testByChannel("2.zip", "22.zip", true);
//        testByChannel("3.txt", "33.txt", true);
    }

    @Test
    public void testAsyncFuture() {
        long stm = System.currentTimeMillis();
        CopyFile cf = new CopyFile();
        if (cf.copyByAsyncFuture("1.exe", "111.exe", true)) {
            System.out.println("1ok");
        } else {
            System.out.println(cf.getErrMsg());
        }

        if (cf.copyByAsyncFuture("2.zip", "222.zip", true)) {
            System.out.println("2ok");
        } else {
            System.out.println(cf.getErrMsg());
        }
        if (cf.copyByAsyncFuture("3.txt", "333.txt", true)) {
            System.out.println("3ok");
        } else {
            System.out.println(cf.getErrMsg());
        }
        System.out.println(System.currentTimeMillis() - stm);
    }

    @Test
    public void testAsyncCallback() {
        long stm = System.currentTimeMillis();
        CopyFile cf = new CopyFile();
        if (cf.copyByAsyncCallback("1.exe", "1111.exe", true, 20000)) {
            System.out.println("1ok");
        } else {
            System.out.println(cf.getErrMsg());
        }

        if (cf.copyByAsyncCallback("2.zip", "2222.zip", true, 10000)) {
            System.out.println("2ok");
        } else {
            System.out.println(cf.getErrMsg());
        }
        if (cf.copyByAsyncCallback("3.txt", "3333.txt", true, 5000)) {
            System.out.println("3ok");
        } else {
            System.out.println(cf.getErrMsg());
        }
        System.out.println(System.currentTimeMillis() - stm);
    }
}
