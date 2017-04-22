/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package com.dp.examples;

import jshellsession.CommandOutputStream;
import jshellsession.Config;
import jshellsession.OnCommandOutputListener;

import java.io.IOException;

public class CommandOutputStreamExample {

    public static void main(String[] args) {
        try {
            final CommandOutputStream stream = new CommandOutputStream(Config.defaultConfig());
            stream.stdOutStream("top -b -d 1", new OnCommandOutputListener() {
                @Override
                public void onNewStdOutLine(String line) {
                    System.out.println(line);
                }

                @Override
                public void onNewErrOutLine(String line) {
                    System.err.println(line);
                }
            });
            stream.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
