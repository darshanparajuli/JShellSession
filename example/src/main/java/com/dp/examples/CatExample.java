package com.dp.examples;

import jshellsession.CommandResult;
import jshellsession.Config;
import jshellsession.JShellSession;

import java.io.IOException;
import java.util.Arrays;

public class CatExample {

    public static void main(String[] args) throws IOException {
        final JShellSession shellSession = new JShellSession(Config.defaultConfig());

        final CommandResult result = shellSession.run("ls /proc | sort");
        System.out.println("ls result: " + Arrays.toString(result.stdOut()));

        for (String s : result.stdOut()) {
            if (isInteger(s)) {
                final CommandResult r = shellSession.run("cat /proc/" + s + "/cmdline");
                System.out.println(s + ": " + Arrays.toString(r.stdOut()));
            }
        }

        shellSession.close();
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
