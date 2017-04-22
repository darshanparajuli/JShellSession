package jshellsession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Config {

    String mShellCommand;
    Map<String, String> mEnv;
    Set<Integer> mSuccessExitValues;
    boolean mRedirectErrorStream;

    private Config() {
        mShellCommand = "sh"; // default
        mEnv = new HashMap<>();
        mSuccessExitValues = new HashSet<>();
        mRedirectErrorStream = false;
    }

    public static Config defaultConfig() {
        return new Config();
    }

    public static class Builder {

        private Config mConfig;

        public Builder() {
            mConfig = new Config();
        }

        public Builder setShellCommand(String shellCommand) {
            mConfig.mShellCommand = shellCommand;
            return this;
        }

        public Builder setRedirectErrorStream(boolean redirectErrorStream) {
            mConfig.mRedirectErrorStream = redirectErrorStream;
            return this;
        }

        public Builder addEnvVariable(String var, String value) {
            mConfig.mEnv.put(var, value);
            return this;
        }

        public Builder addExitSuccessValue(int exitSuccessValue) {
            mConfig.mSuccessExitValues.add(exitSuccessValue);
            return this;
        }

        public Config build() {
            return mConfig;
        }
    }

}