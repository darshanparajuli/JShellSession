/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

import java.io.IOException;
import java.io.Reader;

public class CommandOutputReader extends Reader {

    private String[] mOutput;
    private int mCharIndex;
    private int mArrayIndex;
    private JShellSession mSession;

    public CommandOutputReader(String cmd) throws IOException {
        this(Config.defaultConfig(), cmd);
    }

    public CommandOutputReader(Config config, String cmd) throws IOException {
        this(new JShellSession(config), cmd);
    }

    public CommandOutputReader(JShellSession shellSession, String cmd) throws IOException {
        if (shellSession == null) {
            throw new IOException("shellSession is null");
        }
        mSession = shellSession;
        mOutput = mSession.run(cmd).stdOut();
        mCharIndex = 0;
        mArrayIndex = 0;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int read;
        if (mArrayIndex == mOutput.length) {
            read = -1;
        } else {
            read = 0;
            for (int i = 0; i < len; i++) {
                if (mArrayIndex == mOutput.length) {
                    break;
                }

                if (mCharIndex == mOutput[mArrayIndex].length()) {
                    cbuf[off + i] = '\n';
                    mCharIndex = 0;
                    mArrayIndex++;
                } else {
                    cbuf[off + i] = mOutput[mArrayIndex].charAt(mCharIndex);
                    mCharIndex++;
                }
                read++;
            }
        }
        return read;
    }

    @Override
    public void close() {
        mSession.close();
        mOutput = null;
    }
}
