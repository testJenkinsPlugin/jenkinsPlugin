package com.amcbridge.jenkins.plugins.xmlSerialization;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("config")
public class Config {

    @XStreamAsAttribute
    private String builder, platform, userConfig, configuration;

    public Config() {
    }

    public Config(String configuration, String builder, String platform) {
        this.builder = builder;
        this.configuration = configuration;
        this.platform = platform;
    }

    public void setBuilder(String value) {
        builder = value;
    }

    public String getBuilder() {
        return builder;
    }

    public void setConfigurator(String value) {
        configuration = value;
    }

    public String getConfigurator() {
        return configuration;
    }

    public void setPlatform(String value) {
        platform = value;
    }

    public String getPlatform() {
        return platform;
    }

    public void setUserConfig(String value) {
        userConfig = value;
    }

    public String getUserConfig() {
        return userConfig;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof Config)) {
            return false;
        }
        Config other = (Config) obj;
        return (this.builder.equals(other.getBuilder())
                && this.platform.equals(other.getPlatform())
                && this.userConfig != null
                && other.getUserConfig() != null
                && this.userConfig.equals(other.getUserConfig())
                && this.configuration.equals(other.configuration));
    }
}
