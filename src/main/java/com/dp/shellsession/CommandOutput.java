package com.dp.shellsession;/*
 * Copyright (c) 2017 Darshan Parajuli
 */

public class CommandOutput {

    private int mExitCode;
    private String[] mStdOut, mErrOut;

    CommandOutput(int exitCode) {
        this(exitCode, new String[]{}, new String[]{});
    }

    CommandOutput(int exitCode, String[] stdOut, String[] errOut) {
        mExitCode = exitCode;
        mStdOut = stdOut;
        mErrOut = errOut;
    }

    public String[] stdOut() {
        return mStdOut;
    }

    public String[] errOut() {
        return mErrOut;
    }

    public int exitCode() {
        return mExitCode;
    }

    public boolean exitSuccess() {
        return mExitCode == 0;
    }
}
