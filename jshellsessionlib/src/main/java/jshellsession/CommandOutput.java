/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.util.Set;

public class CommandOutput {

    private int mExitCode;
    private String[] mStdOut, mErrOut;
    private Set<Integer> mSuccessExitValues;

    CommandOutput(int exitCode, Set<Integer> successExitValues) {
        this(exitCode, successExitValues, new String[]{}, new String[]{});
    }

    CommandOutput(int exitCode, Set<Integer> successExitValues, String[] stdOut, String[] errOut) {
        mExitCode = exitCode;
        mSuccessExitValues = successExitValues;
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
        if (mSuccessExitValues.isEmpty()) {
            return mExitCode == 0;
        } else {
            return mSuccessExitValues.contains(mExitCode);
        }
    }

    public boolean exitFailure() {
        return !exitSuccess();
    }
}
