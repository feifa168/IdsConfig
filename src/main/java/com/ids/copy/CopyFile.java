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
    private class CopyInfo {
        public CopyStatus status = CopyStatus.DEFAULT;
        public boolean isnotify = false;
    }
    //private CopyStatus status = CopyStatus.DEFAULT;

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

            long size = fi.size();
            long pos = 0L;
            long bytesCopied;
            for(long count = 0L; pos < size; pos += bytesCopied) {
                long remain = size - pos;
                count = remain > 31457280L ? 31457280L : remain;
                bytesCopied = fo.transferFrom(fi, pos, count);
                if (bytesCopied == 0L) {
                    break;
                }
            }

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

    public boolean copyFolder(String srcPath, String dstPath) {
        return false;
    }

    // 效率没有copyByChannel高，拷贝大文件不稳定，有时有异常，暂不推荐使用，后期优化
    public boolean copyByAsyncFuture(String srcFile, String dstFile, boolean isOverride) {
        if(!ifOveride(dstFile, isOverride)) {
            return false;
        }

        Path p = Paths.get(srcFile);
        AsynchronousFileChannel asfcSrc = null;
        CopyStatus curStatus = CopyStatus.DEFAULT;
        try {
            asfcSrc = AsynchronousFileChannel.open(Paths.get(srcFile), StandardOpenOption.READ);

            ByteBuffer buf = ByteBuffer.allocate((int)asfcSrc.size());
            Future<Integer> rd = asfcSrc.read(buf, 0);
            while (true) {
                if (rd.isDone()) {
                    curStatus = CopyStatus.READFINISHED;
                    AsynchronousFileChannel asfcDst = null;
                    try {
                        asfcDst = AsynchronousFileChannel.open(Paths.get(dstFile), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        buf.flip();
                        Future<Integer> wt = asfcDst.write(buf, 0);
                        while(true) {
                            if (wt.isDone()) {
                                curStatus = CopyStatus.WRITEFINISHED;
                                break;
                            } else if (wt.isCancelled()) {
                                errMsg = "write file " + dstFile + " is cancelled";
                                curStatus = CopyStatus.WRITEFAIL;
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

                    break;
                }
                if (rd.isCancelled()) {
                    errMsg = "read file " + srcFile + " is cancelled";
                    curStatus = CopyStatus.READFAIL;
                    break;
                }
            }
        } catch (IOException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        } finally {
            if (asfcSrc != null) {
                try {
                    asfcSrc.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    errMsg = e.getMessage();
                }
            }
        }

        return (curStatus == CopyStatus.WRITEFINISHED);
    }

    // 效率没有copyByChannel高，拷贝大文件不稳定，有时有异常，暂不推荐使用，后期优化
    public boolean copyByAsyncCallback(String srcFile, String dstFile, boolean isOverride, int waitMillisecond) {
        if(!ifOveride(dstFile, isOverride)) {
            return false;
        }

        CopyInfo   copyInfo = new CopyInfo();
        Path p = Paths.get(srcFile);
        AsynchronousFileChannel asfcSrc = null;
        try {
            asfcSrc = AsynchronousFileChannel.open(Paths.get(srcFile), StandardOpenOption.READ);
            AsynchronousFileChannel asfcDst = AsynchronousFileChannel.open(Paths.get(dstFile), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            ByteBuffer buf = ByteBuffer.allocate((int)asfcSrc.size());
            asfcSrc.read(buf, 0, buf, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    synchronized (copyInfo) {
                        copyInfo.status = CopyStatus.READFINISHED;
                    }
                    //AsynchronousFileChannel asfcDst = null;
                    //AsynchronousFileChannel asfcDst = AsynchronousFileChannel.open(Paths.get(dstFile), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    buf.flip();
                    asfcDst.write(buf, 0, buf, new CompletionHandler<Integer, ByteBuffer>() {
                        private void close(CopyStatus status) {
                            synchronized (copyInfo) {
                                copyInfo.status = status;
                                copyInfo.isnotify = true;
                                copyInfo.notify();
                            }

                            if (asfcDst != null) {
                                try {
                                    asfcDst.close();
                                } catch (IOException e) {
                                    //e.printStackTrace();
                                    errMsg = e.getMessage();
                                }
                            }
                        }

                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            close(CopyStatus.WRITEFINISHED);
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            errMsg = "write file " + dstFile + " is cancelled";
                            close(CopyStatus.WRITEFAIL);
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    synchronized (copyInfo) {
                        errMsg = "read file " + srcFile + " is cancelled";
                        copyInfo.status = CopyStatus.READFAIL;
                        copyInfo.isnotify = true;
                        copyInfo.notify();
                    }
                }
            });
        } catch (IOException e) {
            //e.printStackTrace();
            errMsg = e.getMessage();
        } finally {
            if (asfcSrc != null) {
                try {
                    asfcSrc.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                    errMsg = e.getMessage();
                }
            }
        }

        synchronized (copyInfo) {
            while (!copyInfo.isnotify) {
                try {
                    copyInfo.wait(waitMillisecond);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    errMsg = e.getMessage();
                }
            }
        }

        return (copyInfo.status == CopyStatus.WRITEFINISHED);
    }
}
