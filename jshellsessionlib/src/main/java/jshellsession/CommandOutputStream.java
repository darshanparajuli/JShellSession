/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.io.Closeable;
import java.io.IOException;

public class CommandOutputStream implements Closeable {

    private JShellSession mShell;
    private Config mConfig;

    public CommandOutputStream(Config config) throws IOException {
        mConfig = config;
        mShell = null;
    }

    public void start(OnCommandOutputListener listener) throws IOException {
        if (mShell != null) {
            throw new IllegalStateException("CommandOutputStream object cannot be reused");
        }

        mShell = new JShellSession(mConfig, listener);
    }

    public boolean isAlive() {
        return mShell != null && mShell.isRunning();
    }

    @Override
    public void close() {
        if (mShell != null) {
            mShell.close();
        }
    }
}
