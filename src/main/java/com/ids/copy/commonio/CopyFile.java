package com.ids.copy.commonio;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class CopyFile {

    private String errMsg = null;
    public String getErrMsg() { return  errMsg; }

    private boolean ifOveride(String file, boolean isOverride) {
        File fl = new File(file);
        if (fl.exists() && fl.isFile()) {
            if (isOverride) {
                fl.delete();
            } else {
                errMsg = "file is exist";
                return false;
            }
        }
        return true;
    }

    public boolean copyFile(String srcFile, String dstFile, boolean isOverride) {
        if(!ifOveride(dstFile, isOverride)) {
            return false;
        }

        try {
            FileUtils.copyFile(new File(srcFile), new File(dstFile));
            return true;
        } catch (IOException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        }
        return false;
    }

    public boolean copyDirectory(String srcDir, String destDir) {
        try {
            FileUtils.copyDirectory(new File(srcDir), new File(destDir));
            return true;
        } catch (IOException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        }
        return  false;
    }
}
