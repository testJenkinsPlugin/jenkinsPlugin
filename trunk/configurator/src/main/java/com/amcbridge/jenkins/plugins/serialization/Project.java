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
    private String pathToFile, localDirectory;
    private Repository repository;
    private PathToArtefacts pathToArtefacts;
    private VersionFile versionFiles;
    private List<Config> configs;

    public Project() {
        this.configs = Lists.newLinkedList();
    }

    public void setPathToArtefacts(PathToArtefacts pathToArtefacts) {
        this.pathToArtefacts = pathToArtefacts;
    }

    public PathToArtefacts getPathToArtefacts() {
        return pathToArtefacts;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setVersionFiles(VersionFile versionFiles) {
        this.versionFiles = versionFiles;
    }

    public VersionFile getVersionFiles() {
        return versionFiles;
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
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
