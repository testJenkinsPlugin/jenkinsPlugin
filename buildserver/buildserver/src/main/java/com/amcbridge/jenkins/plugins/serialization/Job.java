package com.amcbridge.jenkins.plugins.serialization;

import com.google.common.collect.Lists;
import java.util.List;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XStreamAlias("job")
public class Job {

    @XStreamAsAttribute
    private String name;

    private List<Project> projects;

    private String[] scripts, buildMachineConfiguration;

    public Job() {
        this.projects = Lists.newLinkedList();
    }

    public Job(String name) {
        this.name = name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getName() {
        return name;
    }

    public void setProjects(List<Project> value) {
        projects = value;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setBuildMachineConfiguration(String[] value) {
        buildMachineConfiguration = value;
    }

    public String[] getBuildMachineConfigurstion() {
        return buildMachineConfiguration;
    }

    public void setScripts(String[] value) {
        scripts = value;
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