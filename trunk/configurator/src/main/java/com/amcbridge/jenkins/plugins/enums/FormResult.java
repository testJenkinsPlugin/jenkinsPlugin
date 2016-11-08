package com.amcbridge.jenkins.plugins.enums;

public enum FormResult {

    CREATE("create"),
    EDIT("edit"),
    APPROVED("approved"),
    REJECT("reject"),
    CANCEL("cancel");

    private final String resultValue;

    FormResult(String value) {
        this.resultValue = value;
    }

    @Override
    public String toString() {
        return resultValue;
    }
}
