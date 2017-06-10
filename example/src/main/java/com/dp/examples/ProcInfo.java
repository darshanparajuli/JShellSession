package com.dp.examples;

import jshellsession.Config;
import jshellsession.JShellSession;

import java.io.IOException;
import java.util.*;

public class ProcInfo {

    public static void main(String[] args) {
        final Map<Integer, String> map = new HashMap<>();
        try (final JShellSession session = new JShellSession(Config.defaultConfig())) {
            for (String s : session.run("ls -1 /proc").stdOut()) {
                try {
                    final int pid = Integer.parseInt(s);
                    final String cmdlinePath = String.format("/proc/%s/cmdline", s);
                    final String[] stdOut = session.run("cat " + cmdlinePath).stdOut();
                    if (stdOut.length > 0) {
                        final String cmdline = stdOut[0].replace('\0', ' ').trim();
                        map.put(pid, cmdline);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final List<Integer> pids = new ArrayList<>(map.keySet());
        Collections.sort(pids);
        for (int pid : pids) {
            System.out.printf("%6d => %s\n", pid, map.get(pid));
        }
    }

}
