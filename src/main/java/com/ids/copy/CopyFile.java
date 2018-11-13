package com.ids.copy;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

public class CopyFile {
    private String errMsg = null;
    public enum CopyStatus {
        DEFAULT,
        READFAIL,
        WRITEFAIL,
        READFINISHED,
        WRITEFINISHED
    }
    public class CopyInfo {
        CopyStatus status = CopyStatus.DEFAULT;
    }
    //private CopyStatus status = CopyStatus.DEFAULT;
    private CopyInfo   copyInfo = new CopyInfo();

    public String getErrMsg() {
        return errMsg;
    }

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

    public boolean copyByChannel(String srcFile, String dstFile, boolean isOverride) {
        if(!ifOveride(dstFile, isOverride)) {
            return false;
        }

        try {
            FileChannel fi = new FileInputStream(srcFile).getChannel();

            FileChannel fo = FileChannel.open(Paths.get(dstFile), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            fo.transferFrom(fi, 0, fi.size());

            return true;
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        } catch (IOException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        }
        return false;
    }

    public boolean copyByAsync(String srcFile, String dstFile, boolean isOverride) {
        if(!ifOveride(dstFile, isOverride)) {
            return false;
        }

        Path p = Paths.get(srcFile);
        AsynchronousFileChannel asfcSrc = null;
        try {
            asfcSrc = AsynchronousFileChannel.open(Paths.get(srcFile), StandardOpenOption.READ);

            ByteBuffer buf = ByteBuffer.allocate((int)asfcSrc.size());
            asfcSrc.read(buf, 0, buf, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    synchronized (copyInfo) {
                        copyInfo.status = CopyStatus.READFINISHED;
                    }
                    AsynchronousFileChannel asfcDst = null;
                    try {
                        asfcDst = AsynchronousFileChannel.open(Paths.get(dstFile), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        buf.flip();
                        Future<Integer> wt = asfcDst.write(buf, 0);
                        while(true) {
                            if (wt.isDone()) {
                                synchronized (copyInfo) {
                                    copyInfo.status = CopyStatus.WRITEFINISHED;
                                    copyInfo.notify();
                                }
                                break;
                            } else if (wt.isCancelled()) {
                                synchronized (copyInfo) {
                                    copyInfo.status = CopyStatus.WRITEFAIL;
                                    copyInfo.notify();
                                }
                                break;
                            }
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                        errMsg = e.getMessage();
                    } finally {
                        if (asfcDst != null) {
                            try {
                                asfcDst.close();
                            } catch (IOException e) {
                                //e.printStackTrace();
                                errMsg = e.getMessage();
                            }
                        }
                    }

                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    synchronized (copyInfo) {
                        copyInfo.status = CopyStatus.READFAIL;
                        copyInfo.notify();
                    }
                }
            });
        } catch (IOException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        }

        synchronized (copyInfo) {
            try {
                copyInfo.wait(1000*60*5);
            } catch (InterruptedException e) {
                //e.printStackTrace();
                errMsg = e.getMessage();
            }
        }

        if (asfcSrc != null) {
            try {
                asfcSrc.close();
            } catch (IOException e) {
                //e.printStackTrace();
                errMsg = e.getMessage();
            }
        }

        return (copyInfo.status == CopyStatus.WRITEFINISHED);
    }
}
