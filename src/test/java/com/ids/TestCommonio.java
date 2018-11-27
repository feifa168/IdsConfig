package com.ids;

import com.ids.copy.commonio.CopyFile;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class TestCommonio {
    @Test
    public void testNormalCopyFile() {
        try {
            FileOutputStream fos = new FileOutputStream("123.txt");
            FileChannel fc = fos.getChannel();
            try {
                long s = fc.size();
                long pos = fc.position();
                int sldfj = 123;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void testByChannel(String srcFile, String dstFile, boolean isOveride) {
        CopyFile cf = new CopyFile();
        long stm = System.currentTimeMillis();
        if (cf.copyFile(srcFile, dstFile, isOveride)) {
            System.out.println("ok");
        } else {
            System.out.println(cf.getErrMsg());
        }
        System.out.println(System.currentTimeMillis() - stm);
    }
    @Test
    public void testCopyFile() {
        //for (int i=0; i<10; i++)
            testByChannel("2.zip", "test2.zip", true);
//        testByChannel("1.exe", "test1.exe", true);
//        testByChannel("2.zip", "test2.zip", true);
//        testByChannel("3.txt", "test3.txt", true);
    }

    @Test
    public void testDir() {
        CopyFile cf = new CopyFile();
        cf.copyDirectory("E:\\lua", "E:\\test\\lua");
    }
}
