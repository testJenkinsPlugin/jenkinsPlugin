package com.amcbridge.jenkins.plugins.enums;

public enum Configuration {

    RELEASE("Release"),
    DEBUG("Debug"),
    OTHER("Other");

    private final String configurationValue;

    Configuration(String value) {
        this.configurationValue = value;
    }

    @Override
    public String toString() {
        return configurationValue;
    }
}
