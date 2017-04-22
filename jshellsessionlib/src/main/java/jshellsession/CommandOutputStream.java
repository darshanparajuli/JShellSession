/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.io.IOException;

public class CommandOutputStream {

    private JShell mShell;

    public CommandOutputStream(Config config) throws IOException {
        mShell = new JShell(config);
    }

    public void stdOutStream(String cmd, OnCommandOutputListener listener) throws IOException {
        stdOutStream(cmd, 0, listener);
    }

    public void stdOutStream(String cmd, long timeout, OnCommandOutputListener listener) throws IOException {
        mShell.setOnCommandOutputListener(listener);
        mShell.run(cmd, timeout);
    }

    public void close() {
        mShell.close();
    }
}
