package com.amcbridge.jenkins.plugins.serialization;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    public void setConfiguration(String value) {
        configuration = value;
    }

    public String getConfiguration() {
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Config)) {
            return false;
        }
        Config other = Config.class.cast(obj);
        return new EqualsBuilder()
                .append(this.builder, other.builder)
                .append(this.platform, other.platform)
                .append(this.userConfig, other.userConfig)
                .append(this.configuration, other.configuration)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.builder)
                .append(this.platform)
                .append(this.userConfig)
                .append(this.configuration)
                .toHashCode();
    }
}
