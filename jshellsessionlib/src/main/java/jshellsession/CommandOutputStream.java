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

    public void stdOutStream(String cmd, OnCommandOutputListener listener) throws IOException {
        stdOutStream(cmd, 0, listener);
    }

    public void stdOutStream(final String cmd, final long timeout, OnCommandOutputListener listener) throws IOException {
        if (mThread != null) {
            throw new IllegalStateException("Object cannot be reused");
        }

        mShell.setOnCommandOutputListener(listener);
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mShell.run(cmd, timeout);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mThread.start();
    }

    @Override
    public void close() {
        mShell.close();
        if (mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
