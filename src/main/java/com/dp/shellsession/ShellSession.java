package com.dp.shellsession;/*
 * Copyright (c) 2017 Darshan Parajuli
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ShellSession {

    private static final String END_MARKER = "[>>END<<]:";

    private static ShellSession sInstance;

    static {
        sInstance = null;
    }

    private final Object mStdOutConsumerLock = new Object();

    private Lock mLock;
    private Process mProcess;
    private BufferedWriter mWriter;
    private InputStreamReader mStdOutReader;
    private InputStreamReader mStdErrReader;

    private volatile boolean mDoneConsumingStdOut;

    private List<String> mStdOut;
    private List<String> mStdErr;
    private int mExitCode;

    private Thread mThreadStdOut;
    private Thread mThreadStdErr;

    private ShellSession(String shellCmd, Map<String, String> env) throws IOException {
        mStdOut = new ArrayList<>();
        mStdErr = new ArrayList<>();
        mLock = new ReentrantLock();
        mExitCode = 0;
        mDoneConsumingStdOut = false;
        final ProcessBuilder processBuilder = new ProcessBuilder(shellCmd);
        processBuilder.environment().putAll(env);
        mProcess = processBuilder.start();
        mWriter = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));
        mStdOutReader = new InputStreamReader(mProcess.getInputStream());
        mStdErrReader = new InputStreamReader(mProcess.getErrorStream());
        mThreadStdOut = new Thread(new Runnable() {
            @Override
            public void run() {
                processStdOutput();
            }
        });
        mThreadStdErr = new Thread(new Runnable() {
            @Override
            public void run() {
                processErrOutput();
            }
        });

        mThreadStdOut.start();
        mThreadStdErr.start();
    }

    public static boolean init(String shellCmd, Map<String, String> env) {
        if (sInstance == null) {
            try {
                sInstance = new ShellSession(shellCmd, env);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean init(String shellCmd) {
        return init(shellCmd, new HashMap<String, String>());
    }

    public static ShellSession getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("com.dp.shellsession.ShellSession is not available; " +
                    "did you forget to call com.dp.shellsession.ShellSession.init()?");
        }
        return sInstance;
    }

    public static void destroy() {
        if (sInstance != null) {
            sInstance.killProcess();
            sInstance = null;
        }
    }

    public CommandOutput run(String cmd) throws IOException {
        return run(cmd, 0);
    }

    public CommandOutput run(String cmd, long timeout) throws IOException {
        mLock.lock();
        try {
            if (mProcess == null) {
                throw new IllegalStateException("session has been closed");
            }

            if (cmd.equals("exit")) {
                killProcess();
                return new CommandOutput(0);
            }

            synchronized (mStdOutConsumerLock) {
                mDoneConsumingStdOut = false;
            }

            mStdOut.clear();
            mStdErr.clear();

            mWriter.write(String.format("%s; echo \"%s\"$?", cmd, END_MARKER));
            mWriter.newLine();
            mWriter.flush();

            synchronized (mStdOutConsumerLock) {
                while (!mDoneConsumingStdOut) {
                    try {
                        mStdOutConsumerLock.wait(timeout);
                    } catch (InterruptedException ignored) {
                    }
                }
            }

            return new CommandOutput(mExitCode, mStdOut.toArray(new String[mStdOut.size()]),
                    mStdErr.toArray(new String[mStdErr.size()]));
        } finally {
            mLock.unlock();
        }
    }

    private void processErrOutput() {
        try {
            StringBuilder builder = new StringBuilder();
            for (int i = mStdErrReader.read(); i != -1; i = mStdErrReader.read()) {
                final char c = (char) i;
                if (c == '\n' || c == '\0') {
                    final String line = builder.toString().trim();
                    mStdErr.add(line);
                    builder = new StringBuilder();
                } else {
                    builder.append(c);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void processStdOutput() {
        try {
            StringBuilder builder = new StringBuilder();
            for (int i = mStdOutReader.read(); i != -1; i = mStdOutReader.read()) {
                final char c = (char) i;
                if (c == '\n' || c == '\0') {
                    final String line = builder.toString().trim();
                    if (line.contains(END_MARKER)) {
                        if (!line.startsWith(END_MARKER)) {
                            mStdOut.add(line.substring(0, line.indexOf(END_MARKER)));
                        }
                        mExitCode = Integer.parseInt(line.substring(line.indexOf(":") + 1));
                        synchronized (mStdOutConsumerLock) {
                            mDoneConsumingStdOut = true;
                            mStdOutConsumerLock.notify();
                        }
                    } else {
                        mStdOut.add(line);
                    }
                    builder = new StringBuilder();
                } else {
                    builder.append(c);
                }
            }
        } catch (IOException ignored) {
        }
    }

    public boolean isRunning() {
        if (mProcess == null) {
            return false;
        }
        try {
            mProcess.exitValue();
            return false;
        } catch (IllegalThreadStateException ignored) {
            return true;
        }
    }

    private void killProcess() {
        if (mProcess != null) {
            mProcess.destroy();
            mProcess = null;
        }

        if (mStdOutReader != null) {
            try {
                mStdOutReader.close();
            } catch (IOException ignored) {
            }
        }

        if (mWriter != null) {
            try {
                mWriter.close();
            } catch (IOException ignored) {
            }
        }

        try {
            mThreadStdOut.join(1000);
        } catch (InterruptedException ignored) {
        }

        try {
            mThreadStdErr.join(1000);
        } catch (InterruptedException ignored) {
        }
    }
}
