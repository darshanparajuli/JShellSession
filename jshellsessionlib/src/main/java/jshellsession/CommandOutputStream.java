/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommandOutputStream {

    private JShell mShell;

    public CommandOutputStream(String shell) throws IOException {
        this(shell, new HashMap<String, String>());
    }

    public CommandOutputStream(String shell, Map<String, String> env) throws IOException {
        mShell = new JShell(shell, env);
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
