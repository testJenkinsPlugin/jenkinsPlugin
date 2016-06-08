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

}
