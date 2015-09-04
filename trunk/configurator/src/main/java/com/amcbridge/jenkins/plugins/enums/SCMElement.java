package com.amcbridge.jenkins.plugins.enums;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("scm")
public class SCMElement {

    @XStreamAsAttribute
    private String key, value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
