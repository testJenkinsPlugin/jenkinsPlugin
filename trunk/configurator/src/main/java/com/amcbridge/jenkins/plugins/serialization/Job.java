package com.amcbridge.jenkins.plugins.serialization;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XStreamAlias("job")
public class Job {

    @XStreamAsAttribute
    private String name;

    private List<Project> projects;

    private String[] scripts;

    private Map<String, Boolean> buildMachineConfiguration;

    public Job() {
        this.projects = Lists.newLinkedList();
    }

    public Job(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public Map<String, Boolean> getBuildMachineConfiguration() {
        return buildMachineConfiguration;
    }

    public void setBuildMachineConfiguration(Map<String, Boolean> buildMachineConfiguration) {
        this.buildMachineConfiguration = buildMachineConfiguration;
    }

    public void setScripts(String[] scripts) {
        this.scripts = scripts;
    }

    public String[] getScripts() {
        return scripts;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Job)) {
            return false;
        }
        Job other = Job.class.cast(obj);
        return new EqualsBuilder()
                .append(this.name, other.name)
                .append(this.projects, other.projects)
                .append(this.scripts, other.scripts)
                .append(this.buildMachineConfiguration, other.buildMachineConfiguration)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.name)
                .append(this.projects)
                .append(this.scripts)
                .append(this.buildMachineConfiguration)
                .toHashCode();
    }
}
