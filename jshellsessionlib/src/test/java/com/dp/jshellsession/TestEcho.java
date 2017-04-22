/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package com.dp.jshellsession;

import jshellsession.CommandOutput;
import jshellsession.Config;
import jshellsession.JShell;
import jshellsession.JShellSession;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestEcho {

    @Test
    public void testEchoHelloWorld() throws IOException {
        Assert.assertTrue(JShellSession.init(Config.defaultConfig()));
        final JShell jShellSession = JShellSession.getInstance();
        final CommandOutput output = jShellSession.run("echo hello world");
        Assert.assertTrue(output.exitSuccess());
        Assert.assertTrue(output.stdOut().length == 1);
        Assert.assertTrue(output.stdOut()[0].equals("hello world"));
        JShellSession.destroy();
    }

    @Test
    public void testQuickRun() throws IOException {
        final CommandOutput output = JShellSession.quickRun(Config.defaultConfig(), "echo hello world");
        Assert.assertTrue(output.exitSuccess());
        Assert.assertTrue(output.stdOut().length == 1);
        Assert.assertTrue(output.stdOut()[0].equals("hello world"));
    }

}
