package com.amcbridge.jenkins.plugins.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.kohsuke.stapler.DataBoundConstructor;
import com.amcbridge.jenkins.plugins.enums.Configuration;

public class BuilderConfigModel {

    private String builder;
    private String platform;
    private String userConfig;
    private List<Configuration> configs;
    private String builderArgs;
    private UUID guid;

    @DataBoundConstructor
    public BuilderConfigModel(String builder, String platform, String userConfig,
                              Boolean release, Boolean debug, Boolean other, String builderArgs, String guid) {
        this.configs = new ArrayList<>();
        this.builder = builder;
        this.platform = platform;
        this.userConfig = userConfig;
        this.builderArgs = builderArgs;

        if (guid == null || guid.equals("")) {
            this.guid = UUID.randomUUID();
        } else {
            this.guid = UUID.fromString(guid);
        }
        if (release) {
            configs.add(Configuration.RELEASE);
        }
        if (debug) {
            configs.add(Configuration.DEBUG);
        }
        if (other) {
            configs.add(Configuration.OTHER);
        }
    }

    public BuilderConfigModel(){
        configs = new LinkedList<>();
        configs.add(Configuration.RELEASE);
    }

    public List<Configuration> getConfigs() {
        return configs;
    }

    public Boolean isConfigChecked(String value) {
        Configuration conf = Configuration.valueOf(value);
        if (configs.indexOf(conf) != -1) {
            return true;
        }
        return false;
    }

    public String getBuilder() {
        return builder;
    }

    public void setBuilder(String builder) {
        this.builder = builder;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUserConfig() {
        return userConfig;
    }

    public void setUserConfig(String userConfig) {
        this.userConfig = userConfig;
    }

    public void setConfigs(List<Configuration> configs) {
        this.configs = configs;
    }

    public String getBuilderArgs() {
        return builderArgs;
    }

    public void setBuilderArgs(String builderArgs) {
        this.builderArgs = builderArgs;
    }

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    public String getConfigurationsAsString() {
        StringBuilder stringBuilder = new StringBuilder("Configuration: ");
        if (configs == null) {
            return null;
        }
        for (Configuration configuration : configs) {
            stringBuilder.append("[").append(configuration).append("] ");
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BuilderConfigModel that = (BuilderConfigModel) o;

        if (builder != null ? !builder.equals(that.builder) : that.builder != null) return false;
        if (platform != null ? !platform.equals(that.platform) : that.platform != null) return false;
        if (userConfig != null ? !userConfig.equals(that.userConfig) : that.userConfig != null) return false;
        if (configs != null ? !configs.equals(that.configs) : that.configs != null) return false;
        if (builderArgs != null ? !builderArgs.equals(that.builderArgs) : that.builderArgs != null) return false;
        return guid != null ? guid.equals(that.guid) : that.guid == null;

    }

    @Override
    public int hashCode() {
        int result = builder != null ? builder.hashCode() : 0;
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (userConfig != null ? userConfig.hashCode() : 0);
        result = 31 * result + (configs != null ? configs.hashCode() : 0);
        result = 31 * result + (builderArgs != null ? builderArgs.hashCode() : 0);
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        return result;
    }
}
