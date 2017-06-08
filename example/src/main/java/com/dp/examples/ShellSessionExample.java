/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package com.dp.examples;

import jshellsession.CommandResult;
import jshellsession.Config;
import jshellsession.JShellSession;

import java.io.IOException;
import java.util.Scanner;

public class ShellSessionExample {

    public static void main(String[] args) {
        try (final JShellSession jShellSession = new JShellSession(Config.defaultConfig())) {
            final Scanner scanner = new Scanner(System.in);
            while (jShellSession.isRunning()) {
                System.out.print(">> ");
                try {
                    final CommandResult output = jShellSession.run(scanner.nextLine());
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
            System.out.println("Exit code: " + jShellSession.getExitCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
