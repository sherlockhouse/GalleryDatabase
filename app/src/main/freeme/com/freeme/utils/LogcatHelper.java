package com.freeme.utils;

import android.content.Context;
import android.os.Environment;

import com.freeme.gallery.util.AppConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LogcatHelper {
    private static boolean IsUser = !AppConfig.DEBUG;

    private static LogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private Context mContext;
    private int     mPId;

    private LogcatHelper(Context context) {
        mContext = context;
        if(!IsUser) {
            mPId = android.os.Process.myPid();
            init();
        }
    }

    public static void init() {
        File external = Environment.getExternalStorageDirectory();
        File logDir = new File(external.getAbsolutePath() + File.separator + "FreemeGallery/Logger" + File.separator);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        PATH_LOGCAT = logDir.getPath();
    }

    public static LogcatHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogcatHelper(context);
        }
        return INSTANCE;
    }

    public void start() {
        if (mLogDumper == null && !IsUser) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
            mLogDumper.start();
        }
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    public void sendLogMessage(Context context, String user) {
        if (mLogDumper != null) {
            mLogDumper.setLogFileLock(true);
            String file = mLogDumper.getLogFileName();
            File sendFile = new File(file);
            if (sendFile.exists() && sendFile.length() > 2000) {
                try {
                    //EmailHelper.sendMail(context, user, file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                File newFile = new File(file);
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mLogDumper.setLogFileLock(false);
        }
    }

    private class LogDumper extends Thread {
        private final String mPID;
        String fileName;
        String cmds = null;
        private Process logcatProc;
        private BufferedReader   mReader      = null;
        private boolean          mRunning     = false;
        private FileOutputStream out          = null;
        private List<String>     logsMessage  = new ArrayList<String>();
        private boolean          mLogFileLock = false;
        private String logFileName;

        public LogDumper(String pid, String file) {
            mPID = String.valueOf(pid);
            fileName = file;
            File mFile = new File(fileName, "GalleryLog.txt");
            if (!mFile.exists()) {
                try {
                    mFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                logFileName = mFile.toString();
                out = new FileOutputStream(mFile, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /**
             * 日志等级：*:v  , *:d  , *:w , *:e , *:f  , *:s
             * 显示当前mPID程序的 e等级的日志.
             * */
            cmds = "logcat *:e time &| grep " + mPID;
        }

        public boolean isLogFileLock() {
            return mLogFileLock;
        }

        public void setLogFileLock(boolean lock) {
            mLogFileLock = lock;
        }

        public String getLogFileName() {
            return logFileName;
        }

        public void stopLogs() {
            mRunning = false;
        }

        private boolean checkFileMaxSize(String file) {
            File sizefile = new File(file);
            if (sizefile.exists()) {
                //1.5MB
                if (sizefile.length() > 1572864) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public void run() {
            mRunning = true;
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }

                    if (out != null) {
                        synchronized (out) {
                            boolean maxSize = checkFileMaxSize(getLogFileName());
                            if (maxSize) {
                                //文件大小超过1.5mb
//								//sendLogMessage(mContext, DeviceHelper.getInstance(mContext).getImei());
                            }
                            if (isLogFileLock()) {
                                if (line.contains(mPID)) {
                                    logsMessage.add(line.getBytes() + "\n");
                                }
                            } else {
                                if (logsMessage.size() > 0) {
                                    for (String _log : logsMessage) {
                                        out.write(_log.getBytes());
                                    }
                                    logsMessage.clear();
                                }

                                /**
                                 * 再次过滤日志，筛选当前日志中有 mPID 则是当前程序的日志.
                                 * */
                                if (line.contains(mPID)) {
                                    out.write(line.getBytes());
                                    out.write("\n".getBytes());
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }
}