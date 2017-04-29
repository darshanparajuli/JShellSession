/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package com.dp.jshellsession;

import jshellsession.CommandResult;
import jshellsession.Config;
import jshellsession.JShellSession;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestEcho {

    @Test
    public void testEchoHelloWorld() throws IOException {
        final CommandResult output = JShellSession.quickRun(Config.defaultConfig(), "echo hello world");
        Assert.assertTrue(output.exitSuccess());
        Assert.assertTrue(output.stdOut().length == 1);
        Assert.assertTrue(output.stdOut()[0].equals("hello world"));
    }

}
