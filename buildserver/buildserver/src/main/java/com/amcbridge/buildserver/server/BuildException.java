package com.amcbridge.buildserver.server;

public class BuildException extends Exception {

    public BuildException() {
        super();
    }

    public BuildException(String aMessage) {
        super(aMessage);
    }

    public BuildException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }

    public BuildException(Throwable aCause) {
        super(aCause);
    }
}
