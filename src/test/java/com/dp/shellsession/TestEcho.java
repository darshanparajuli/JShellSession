/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package com.dp.shellsession;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestEcho {

    @Test
    public void testEchoHelloWorld() throws IOException {
        Assert.assertTrue(ShellSession.init("sh"));
        final ShellSession shellSession = ShellSession.getInstance();
        final CommandOutput output = shellSession.run("echo hello world");
        Assert.assertTrue(output.exitSuccess());
        Assert.assertTrue(output.stdOut().length == 1);
        Assert.assertTrue(output.stdOut()[0].equals("hello world"));
        ShellSession.destroy();
    }

}
