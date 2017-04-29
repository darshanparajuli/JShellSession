/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JShellSession {

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
    private Set<Integer> mSuccessExitValues;

    private Thread mThreadStdOut;
    private Thread mThreadStdErr;

    private OnCommandOutputListener mOnCommandOutputListener;

    public JShellSession(Config config) throws IOException {
        mStdOut = new ArrayList<>();
        mStdErr = new ArrayList<>();
        mLock = new ReentrantLock();
        mExitCode = 0;
        mDoneConsumingStdOut = false;
        mOnCommandOutputListener = null;
        mSuccessExitValues = new HashSet<>(config.mSuccessExitValues);

        mProcess = createProcess(config);
        mWriter = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));
        mStdOutReader = new InputStreamReader(mProcess.getInputStream());
        mStdErrReader = new InputStreamReader(mProcess.getErrorStream());

        mThreadStdOut = new Thread(new Runnable() {
            @Override
            public void run() {
                processStdOutput();
            }
        });
        mThreadStdOut.start();

        if (config.mRedirectErrorStream) {
            mThreadStdErr = null;
        } else {
            mThreadStdErr = new Thread(new Runnable() {
                @Override
                public void run() {
                    processErrOutput();
                }
            });
            mThreadStdErr.start();
        }
    }

    private Process createProcess(Config config)
            throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder(config.mShellCommand);
        processBuilder.redirectErrorStream(config.mRedirectErrorStream);
        processBuilder.environment().putAll(config.mEnv);
        return processBuilder.start();
    }

    public CommandResult run(String cmd) throws IOException {
        return run(cmd, 0);
    }

    private String validateCommand(String cmd) {
        cmd = cmd.trim();
        return cmd.isEmpty() ? ":" : cmd;
    }

    public static CommandResult quickRun(String cmd) throws IOException {
        return quickRun(Config.defaultConfig(), cmd);
    }

    public static CommandResult quickRun(Config config, String cmd) throws IOException {
        final JShellSession jShellSession = new JShellSession(config);
        try {
            return jShellSession.run(cmd);
        } finally {
            jShellSession.close();
        }
    }

    public CommandResult run(String cmd, long timeout) throws IOException {
        mLock.lock();
        cmd = validateCommand(cmd);
        try {
            if (mProcess == null) {
                throw new IllegalStateException("session has been closed");
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

            return new CommandResult(mExitCode, mSuccessExitValues, mStdOut.toArray(new String[mStdOut.size()]),
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
                        notifyDoneConsumingStdOut();
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

        if (mProcess != null) {
            try {
                mExitCode = mProcess.waitFor();
            } catch (InterruptedException ignored) {
            }
        }
        notifyDoneConsumingStdOut();
    }

    private void notifyDoneConsumingStdOut() {
        synchronized (mStdOutConsumerLock) {
            mDoneConsumingStdOut = true;
            mStdOutConsumerLock.notify();
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

    public void setOnCommandOutputListener(OnCommandOutputListener listener) {
        mOnCommandOutputListener = listener;
    }

    public void close() {
        if (mProcess != null) {
            mProcess.destroy();
            mProcess = null;

            mThreadStdOut.interrupt();
        }

        if (mStdOutReader != null) {
            try {
                mStdOutReader.close();
            } catch (IOException ignored) {
            }
        }

        if (mStdErrReader != null) {
            try {
                mStdErrReader.close();
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

        if (mThreadStdErr != null) {
            try {
                mThreadStdErr.join(1000);
            } catch (InterruptedException ignored) {
            }
        }

        mOnCommandOutputListener = null;
    }
}
