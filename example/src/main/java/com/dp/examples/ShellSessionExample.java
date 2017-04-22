/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package com.dp.examples;

import jshellsession.CommandOutput;
import jshellsession.Config;
import jshellsession.JShell;
import jshellsession.JShellSession;

import java.io.IOException;
import java.util.Scanner;

public class ShellSessionExample {

    public static void main(String[] args) {
        if (JShellSession.init(Config.defaultConfig())) {
            final JShell jshell = JShellSession.getInstance();

            final Scanner scanner = new Scanner(System.in);
            while (jshell.isRunning()) {
                System.out.print(">> ");
                try {
                    final CommandOutput output = jshell.run(scanner.nextLine());
                    if (output.exitSuccess()) {
                        for (String s : output.stdOut()) {
                            System.out.println(s);
                        }
                    } else {
                        for (String s : output.errOut()) {
                            System.err.println(s);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                    break;
                }
            }

            JShellSession.destroy();
        }
    }

}
