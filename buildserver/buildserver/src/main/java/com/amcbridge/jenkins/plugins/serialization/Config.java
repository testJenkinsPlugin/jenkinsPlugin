package com.amcbridge.jenkins.plugins.serialization;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("config")
public class Config {

    @XStreamAsAttribute
    private String builder;
    private String platform;
    private String userConfig;
    private String configuration;
    private String builderArgs;

    public Config() {
    }

    public Config(String configuration, String builder, String platform, String builderArgs) {
        this.builder = builder;
        this.configuration = configuration;
        this.platform = platform;
    }

    public String getBuilder() {
        return builder;
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getPlatform() {
        return platform;
    }

    public String getUserConfig() {
        return userConfig;
    }

    public String getBuilderArgs() {
        return builderArgs;
    }

    public void setBuilder(String builder) {
        this.builder = builder;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setUserConfig(String userConfig) {
        this.userConfig = userConfig;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public void setBuilderArgs(String builderArgs) {
        this.builderArgs = builderArgs;
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
                .append(this.builderArgs, other.builderArgs)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.builder)
                .append(this.platform)
                .append(this.userConfig)
                .append(this.configuration)
                .append(this.builderArgs)
                .toHashCode();
    }
}
