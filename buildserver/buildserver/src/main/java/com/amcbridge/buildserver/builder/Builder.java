package com.amcbridge.buildserver.builder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("builder")
public class Builder {

    @XStreamAsAttribute
    private String name, architecture, executeBuild, commandLine;

    public String getName() {
        return name;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getExecuteBuild() {
        return executeBuild;
    }

    public String getCommandLine() {
        return commandLine;
    }
}
