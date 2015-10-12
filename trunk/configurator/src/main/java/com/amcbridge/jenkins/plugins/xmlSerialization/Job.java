package com.amcbridge.jenkins.plugins.xmlSerialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.configurationModels.BuilderConfigModel;
import com.amcbridge.jenkins.plugins.configurationModels.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.enums.Configuration;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("job")
public class Job {

    @XStreamAsAttribute
    private String name;

    private List<Project> projects;

    private String[] scripts, buildMachineConfiguration;

    public Job() {
    }

    public Job(String name) {
        this.name = name;
    }

    public Job(BuildConfigurationModel config) {
        name = JobManagerGenerator.validJobName(config.getProjectName());
        buildMachineConfiguration = config.getBuildMachineConfiguration();
        scripts = config.getScripts();

        if (config.getProjectToBuild() != null) {
            projects = new ArrayList<Project>(config.getProjectToBuild().size());
            for (ProjectToBuildModel projectModel : config.getProjectToBuild()) {

                Repository repo = new Repository();
                repo.setType(config.getScm());
                repo.setUrl(projectModel.getProjectUrl());

                PathToArtefacts artefacts = new PathToArtefacts();
                for (String artefactPath : projectModel.getArtefacts()) {
                    artefacts.addFile(artefactPath);
                }

                VersionFile versionFiles = new VersionFile();
                if (versionFiles != null) {
                    for (String versionFilePath : projectModel.getVersionFiles()) {
                        versionFiles.addFile(versionFilePath);
                    }
                    if (!versionFiles.getFiles().isEmpty()) {
                        versionFiles.setIsVersionFile(true);
                    }
                }

                List<Config> configurations = null;
                if (projectModel.getBuilders() != null) {
                    configurations = new ArrayList<Config>(projectModel.getBuilders().length);
                    for (BuilderConfigModel builderModel : projectModel.getBuilders()) {
                        Config newConfig = null;
                        if (builderModel.getConfigs().isEmpty()) {
                            newConfig = new Config();
                            newConfig.setBuilder(builderModel.getBuilder());
                            newConfig.setPlatform(builderModel.getPlatform());
                            configurations.add(newConfig);
                        } else {
                            for (Configuration configEnum : builderModel.getConfigs()) {
                                newConfig = new Config(configEnum.toString(), builderModel.getBuilder(),
                                        builderModel.getPlatform());
                                if (configEnum.equals(Configuration.OTHER)) {
                                    newConfig.setUserConfig(builderModel.getUserConfig());
                                }
                                configurations.add(newConfig);
                            }
                        }
                    }
                }

                Project newProject = new Project();
                newProject.setRepository(repo);
                newProject.setPathToFile(projectModel.getFileToBuild());
                newProject.setLocalDirectory(projectModel.getLocalDirectoryPath() == ""
                        ? null : projectModel.getLocalDirectoryPath());
                newProject.setPathToArtefacts(artefacts);
                newProject.setVersionFiles(versionFiles);
                newProject.setConfigs(configurations);
                projects.add(newProject);

            }
        }
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

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof Job)) {
            return false;
        }
        Job other = (Job) obj;
        return (this.name.equals(other.getName())
                && Arrays.equals(this.scripts, other.getScripts())
                && Arrays.equals(this.buildMachineConfiguration, other.getBuildMachineConfigurstion())
                && this.projects.containsAll(other.getProjects())
                && other.getProjects().containsAll(this.projects));
    }
}
