package com.amcbridge.jenkins.plugins.enums;

public enum MessageDescription {

    CREATE("Configuration has been successfully created!"),
    CHANGE("Configuration has been changed."),
    APPROVE("Configuration has been successfully approved!"),
    REJECT("Configuration has been rejected by administrator. The reasons of rejection are:"),
    MARKED_FOR_DELETION("Configuration has been marked for deletion."),
    DELETE_PERMANENTLY("Configuration has been successfully deleted."),
    RESTORE("Configuration has been successfully restored."),
    COPY("Configuration has been successfully copied from.");

    private final String messageDescriptionValue;

    MessageDescription(String value) {
        this.messageDescriptionValue = value;
    }

    @Override
    public String toString() {
        return messageDescriptionValue;
    }
}
