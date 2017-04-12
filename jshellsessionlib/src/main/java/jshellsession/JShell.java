/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JShell {

    private static final String END_MARKER = "[>>END<<]:";

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

    private OnCommandOutputListener mOnCommandOutputListener;

    JShell(String shell, Map<String, String> env) throws IOException {
        mStdOut = new ArrayList<>();
        mStdErr = new ArrayList<>();
        mLock = new ReentrantLock();
        mExitCode = 0;
        mDoneConsumingStdOut = false;
        mOnCommandOutputListener = null;

        mProcess = createProcess(shell, env);
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

    private Process createProcess(String shell, Map<String, String> env) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder(shell);
        processBuilder.environment().putAll(env);
        return processBuilder.start();
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
                close();
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
                    builder.setLength(0);

                    if (mOnCommandOutputListener != null) {
                        mOnCommandOutputListener.onNewErrOutLine(line);
                    }
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

                        if (mOnCommandOutputListener != null) {
                            mOnCommandOutputListener.onNewStdOutLine(line);
                        }
                    }
                    builder.setLength(0);
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

    void setOnCommandOutputListener(OnCommandOutputListener listener) {
        mOnCommandOutputListener = listener;
    }

    void close() {
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

        mOnCommandOutputListener = null;
    }
}
