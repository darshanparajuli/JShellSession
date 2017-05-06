/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.io.Closeable;
import java.io.IOException;

public class CommandOutputStream implements Closeable {

    private JShellSession mShell;
    private Thread mThread;

    public CommandOutputStream(Config config) throws IOException {
        mShell = new JShellSession(config);
        mThread = null;
    }

    public void stdOutStream(final String cmd, OnCommandOutputListener listener) throws IOException {
        if (mThread != null) {
            throw new IllegalStateException("Object cannot be reused");
        }

        mShell.setOnCommandOutputListener(listener);
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mShell.run(cmd);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mShell.close();
                    mShell = null;
                }
            }
        });
        mThread.start();
    }

    public boolean isStreaming() {
        return mShell != null;
    }

    @Override
    public void close() {
        if (mShell != null) {
            mShell.close();
        }
        if (mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
