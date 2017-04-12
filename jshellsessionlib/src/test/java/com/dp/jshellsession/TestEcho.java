/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package com.dp.jshellsession;

import jshellsession.JShell;
import org.junit.Assert;
import org.junit.Test;
import jshellsession.CommandOutput;
import jshellsession.JShellSession;

import java.io.IOException;

public class TestEcho {

    @Test
    public void testEchoHelloWorld() throws IOException {
        Assert.assertTrue(JShellSession.init("bash"));
        final JShell jShellSession = JShellSession.getInstance();
        final CommandOutput output = jShellSession.run("echo hello world");
        Assert.assertTrue(output.exitSuccess());
        Assert.assertTrue(output.stdOut().length == 1);
        Assert.assertTrue(output.stdOut()[0].equals("hello world"));
        JShellSession.destroy();
    }

    @Test
    public void testQuickRun() throws IOException {
        final CommandOutput output = JShellSession.quickRun("bash", "echo hello world");
        Assert.assertTrue(output.exitSuccess());
        Assert.assertTrue(output.stdOut().length == 1);
        Assert.assertTrue(output.stdOut()[0].equals("hello world"));
    }

}
