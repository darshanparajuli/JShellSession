[![Build Status](https://travis-ci.org/darshanparajuli/JShellSession.svg?branch=master)](https://travis-ci.org/darshanparajuli/JShellSession)
# JShellSession

A simple Java library for running shell commands.

## Simple example
```java
// create new JShellSession object
// Config.defaultConfig().builder() can be used to customize the default Config object
try (final JShellSession session = new JShellSession(Config.defaultConfig())) {
    // run command, i.e "echo hello world"
    final CommandResult result = session.run("echo hello world");

    // check if command ran successfully
    if (result.exitSuccess()) {
        // process stdout or stderr (using result.stdErr())
        for (String s : result.stdOut()) {
            System.out.println(s);
        }
    } else {
        System.err.println("Exit code: " + result.exitCode());
    }
} catch (IOException e) {
    e.printStackTrace();
}
```

###### Any feedback is much appreciated.

:sunglasses: 
