/*
 * Copyright (c) 2017 Darshan Parajuli
 */

package jshellsession;

public interface OnCommandOutputListener {

    void onNewStdOutLine(String line);

    void onNewErrOutLine(String line);
}
