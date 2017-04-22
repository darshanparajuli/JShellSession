/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.io.IOException;

public class JShellSession {

    private static JShell sInstance;

    static {
        sInstance = null;
    }

    private JShellSession() {
        // prevent instantiation
    }

    public static boolean init(Config config) {
        if (sInstance == null) {
            try {
                sInstance = new JShell(config);
            } catch (IOException e) {
                return false;
            }
        }
        return true;
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

    public static CommandOutput quickRun(Config config, String cmd) throws IOException {
        final JShell jshell = new JShell(config);
        try {
            return jshell.run(cmd);
        } finally {
            jshell.close();
        }
    }
}
