/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JShellSession {

    private static JShell sInstance;

    static {
        sInstance = null;
    }

    private JShellSession() {
        // prevent instantiation
    }

    public static boolean init(String shellCmd, Map<String, String> env) {
        if (sInstance == null) {
            try {
                sInstance = new JShell(shellCmd, env);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean init(String shell) {
        return init(shell, new HashMap<String, String>());
    }

    public static JShell getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("JShellSession is not available; " +
                    "did you forget to call JShellSession.init()?");
        }
        return sInstance;
    }

    public static void destroy() {
        if (sInstance != null) {
            sInstance.close();
            sInstance = null;
        }
    }

    public static CommandOutput quickRun(String shell, String cmd) throws IOException {
        final JShell jshell = new JShell(shell, new HashMap<String, String>());
        try {
            return jshell.run(cmd);
        } finally {
            jshell.close();
        }
    }
}
