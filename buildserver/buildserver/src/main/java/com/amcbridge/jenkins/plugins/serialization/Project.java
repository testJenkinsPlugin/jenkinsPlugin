package com.amcbridge.jenkins.plugins.serialization;

import com.google.common.collect.Lists;
import java.util.List;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XStreamAlias("project")
public class Project {

    @XStreamAsAttribute
    private String pathToFile;
    @XStreamAsAttribute
    private String localDirectory;
    @XStreamAsAttribute
    private Repository repository;
    @XStreamAsAttribute
    private PathToArtefacts pathToArtefacts;
    @XStreamAsAttribute
    private VersionFile versionFiles;
    @XStreamAsAttribute
    private List<Config> configs;

    public Project() {
        this.configs = Lists.newLinkedList();
    }

    public void setPathToArtefacts(PathToArtefacts value) {
        pathToArtefacts = value;
    }

    public PathToArtefacts getPathToArtefacts() {
        return pathToArtefacts;
    }

    public void setPathToFile(String value) {
        pathToFile = value;
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(String value) {
        localDirectory = value;
    }

    public void setRepository(Repository value) {
        repository = value;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setVersionFiles(VersionFile value) {
        versionFiles = value;
    }

    public VersionFile getVersionFiles() {
        return versionFiles;
    }

    public void setConfigs(List<Config> value) {
        configs = value;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Config)) {
            return false;
        }
        Project other = Project.class.cast(obj);
        return new EqualsBuilder()
                .append(this.pathToFile, other.pathToFile)
                .append(this.localDirectory, other.localDirectory)
                .append(this.repository, other.repository)
                .append(this.pathToArtefacts, other.pathToArtefacts)
                .append(this.versionFiles, other.versionFiles)
                .append(this.configs, other.configs)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.pathToFile)
                .append(this.localDirectory)
                .append(this.repository)
                .append(this.pathToArtefacts)
                .append(this.versionFiles)
                .append(this.configs)
                .toHashCode();
    }
}
