[![Build Status](https://travis-ci.org/darshanparajuli/JShellSession.svg?branch=master)](https://travis-ci.org/darshanparajuli/JShellSession)
# JShellSession

A simple Java library for running shell commands.

## Usage
1. Initialization (required only once for most cases)
```java
if (!JShellSession.init("bash" /* or "sh" */)) {
    System.err.println("JShellSession initialization failed!");
    System.exit(0);
} 
```
2. Running comands
```java
final JShell jshell = JShellSession.getInstance();
try {
    final CommandOutput output = jshell.run("uptime");
    if (output.exitSuccess()) {
        for (String s : output.stdOut()) {
            System.out.println(s);
            // process stdout line by line
        }
    } else {
        for (String s : output.errOut()) {
            System.out.println(s);
            // process stderr line by line
        }
    }
} catch (IOException e) {
    System.err.println("Error: " + e.getMessage());
}
```
3. And finally destroy the shell session when it's no longer needed (required only once for most cases)
```java
JShellSession.destroy();
```

###### Any feedback is much appreciated.

:sunglasses: 
