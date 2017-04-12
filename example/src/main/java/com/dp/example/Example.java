package com.dp.example;

import shellsession.CommandOutput;
import shellsession.ShellSession;

import java.io.IOException;
import java.util.Scanner;

public class Example {

    public static void main(String[] args) {
        if (ShellSession.init("bash")) {
            final ShellSession shellSession = ShellSession.getInstance();

            final Scanner scanner = new Scanner(System.in);
            while (shellSession.isRunning()) {
                System.out.print(">> ");
                try {
                    final CommandOutput output = shellSession.run(scanner.nextLine());
                    if (output.exitSuccess()) {
                        for (String s : output.stdOut()) {
                            System.out.println(s);
                        }
                    } else {
                        for (String s: output.errOut()) {
                            System.err.println(s);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                    break;
                }
            }

            ShellSession.destroy();
        }
    }

}
