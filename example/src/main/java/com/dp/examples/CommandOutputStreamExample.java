/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package com.dp.examples;

import jshellsession.CommandOutputListenerAdapter;
import jshellsession.CommandOutputStream;
import jshellsession.Config;

import java.io.IOException;

public class CommandOutputStreamExample {

    public static void main(String[] args) {
        try {
            final Config config = Config.defaultConfig()
                    .builder()
                    .setShellCommand("top", "-b", "-d", "1")
                    .build();
            final CommandOutputStream stream = new CommandOutputStream(config);
            stream.start(new CommandOutputListenerAdapter() {
                @Override
                public void onNewStdOutLine(String line) {
                    System.out.println(line);
                }

                @Override
                public void onNewErrOutLine(String line) {
                    System.err.println(line);
                }
            });
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
