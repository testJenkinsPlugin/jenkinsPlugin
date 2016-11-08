package com.amcbridge.jenkins.plugins.messenger;

public interface MessageInfo {

    String getSubject();

    String getDestinationAddress();

    String getMassageText();

    String getCC();
}
