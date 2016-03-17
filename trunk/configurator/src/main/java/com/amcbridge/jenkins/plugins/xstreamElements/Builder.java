package com.amcbridge.jenkins.plugins.xstreamElements;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("builder")
public class Builder {

    @XStreamAsAttribute
    private String key, value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
