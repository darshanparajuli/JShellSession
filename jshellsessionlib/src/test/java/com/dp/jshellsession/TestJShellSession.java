/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package com.dp.jshellsession;

import jshellsession.CommandResult;
import jshellsession.Config;
import jshellsession.JShellSession;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class TestJShellSession {

    @Rule
    public final ExpectedException mExpectedException = ExpectedException.none();

    @Test
    public void testEchoHelloWorld() throws IOException {
        final CommandResult output = JShellSession.quickRun(Config.defaultConfig(), "echo hello world");
        Assert.assertTrue(output.exitSuccess());
        Assert.assertEquals(1, output.stdOut().length);
        Assert.assertEquals("hello world", output.stdOut()[0]);
    }

    @Test
    public void testLsProc() throws IOException {
        final JShellSession shellSession = new JShellSession(Config.defaultConfig());

        Assert.assertTrue(shellSession.isRunning());

        final CommandResult result = shellSession.run("ls -1 .");
        final File[] files = new File(".").listFiles();

        Assert.assertNotNull(files);
        Assert.assertEquals(result.stdOut().length, files.length);

        final Set<String> names = new HashSet<>();
        for (File f : files) {
            names.add(f.getName());
        }

        for (String s : result.stdOut()) {
            Assert.assertTrue(names.contains(s));
        }

        shellSession.close();
        Assert.assertFalse(shellSession.isRunning());
    }

    private boolean isPlatformLinux() {
        final String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("linux");
    }

    @Test
    public void testCmdLine() throws IOException {
        if (!isPlatformLinux()) {
            System.out.println("Skipping testCmdLine() since the OS is not Linux");
            return;
        }

        final JShellSession shellSession = new JShellSession(Config.defaultConfig());

        Assert.assertTrue(shellSession.isRunning());

        final File[] files = new File("/proc")
                .listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        try {
                            Integer.parseInt(name);
                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                });

        Assert.assertNotNull(files);
        for (File f : files) {
            if (!f.exists()) {
                continue;
            }

            final File cmdlineFile = new File(f, "cmdline");
            final BufferedReader reader = new BufferedReader(new FileReader(cmdlineFile));
            String readerResult = reader.readLine();
            if (readerResult == null) {
                readerResult = "";
            } else {
                readerResult = readerResult.replace('\0', ' ').trim();
            }
            reader.close();

            final CommandResult result = shellSession.run("cat " + cmdlineFile.getAbsolutePath());
            final String shellResult = result.stdOut().length == 0 ? "" :
                    result.stdOut()[0].replace('\0', ' ').trim();
            Assert.assertEquals(readerResult, shellResult);
        }

        shellSession.close();
        Assert.assertFalse(shellSession.isRunning());
    }

    @Test
    public void testExitCode() throws IOException {
        final JShellSession jShellSession = new JShellSession(Config.defaultConfig());
        Assert.assertTrue(jShellSession.isRunning());
        jShellSession.run("exit 1");
        Assert.assertFalse(jShellSession.isRunning());
        Assert.assertEquals(1, jShellSession.getExitCode());
        jShellSession.close();
    }

    @Test
    public void testExitCodeException() throws IOException {
        try (final JShellSession jShellSession = new JShellSession(Config.defaultConfig())) {
            Assert.assertTrue(jShellSession.isRunning());

            mExpectedException.expect(IllegalStateException.class);
            jShellSession.getExitCode();
        }
    }

}
