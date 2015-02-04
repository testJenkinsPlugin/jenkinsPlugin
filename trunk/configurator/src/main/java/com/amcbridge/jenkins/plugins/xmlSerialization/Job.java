package com.amcbridge.jenkins.plugins.xmlSerialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.enums.Configuration;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("job")
public class Job
{
	@XStreamAsAttribute
	private String name;

	private List<Project> projects;

	private String[] scripts, buildMachineConfiguration;

	public Job()
	{}
	
	public Job(String name)
	{
		this.name = name;
	}

	public Job (BuildConfigurationModel config)
	{
		Repository repo;
		Project project;
		PathToArtefacts artefacts;
		VersionFile versionFile;
		List<Config> configs;
		Config conf;

		name = JobManagerGenerator.validJobName(config.getProjectName());
		buildMachineConfiguration = config.getBuildMachineConfiguration();
		scripts = config.getScripts();
		if (config.getProjectToBuild() != null)
		{
			projects = new ArrayList<Project>();
			for (int j=0; j<config.getProjectToBuild().size(); j++)
			{
				repo = new Repository();
				project = new Project();

				repo.setType(config.getScm());
				repo.setUrl(config.getProjectToBuild().get(j).getProjectUrl());

				artefacts = new PathToArtefacts();
				for (int k=0; k<config.getProjectToBuild().get(j)
						.getArtefacts().length; k++)
				{
					artefacts.addFile(config.getProjectToBuild().get(j).getArtefacts()[k]);	
				}

				versionFile = new VersionFile();
				if (versionFile != null)
				{
					for (int k=0; k<config.getProjectToBuild()
							.get(j).getVersionFiles().length; k++)
					{
						versionFile.addFile(config.getProjectToBuild()
								.get(j).getVersionFiles()[k]);
					}
					if (versionFile.getFiles().size()>0)
					{
						versionFile.setIsVersionFile(true);
					}
				}

				configs = new ArrayList<Config>();
				if (config.getProjectToBuild().get(j).getBuilders() != null)
				{
					for (int k=0; k<config.getProjectToBuild()
							.get(j).getBuilders().length; k++)
					{
						if (config.getProjectToBuild()
								.get(j).getBuilders()[k].getConfigs().size() <= 0)
						{
							conf = new Config();
							conf.setBuilder(config.getProjectToBuild()
									.get(j).getBuilders()[k].getBuilder());
							conf.setPlatform(config.getProjectToBuild()
									.get(j).getBuilders()[k].getPlatform());
							configs.add(conf);
							continue;
						}
						for (int l=0; l<config.getProjectToBuild().get(j)
								.getBuilders()[k].getConfigs().size(); l++)
						{

							conf = new Config(config.getProjectToBuild().get(j)
									.getBuilders()[k].getConfigs().get(l).toString(),
									config.getProjectToBuild().get(j)
									.getBuilders()[k].getBuilder(),
									config.getProjectToBuild().get(j)
									.getBuilders()[k].getPlatform());
							if (config.getProjectToBuild().get(j)
									.getBuilders()[k].getConfigs().get(l)
									.equals(Configuration.OTHER))
							{
								conf.setUserConfig(config.getProjectToBuild().get(j)
										.getBuilders()[k].getUserConfig());
							}
							configs.add(conf);
						}
					}
				}

				project.setRepository(repo);
				project.setPathToFile(config.getProjectToBuild().get(j).getFileToBuild());
				project.setPathToArtefacts(artefacts);
				project.setVersionFiles(versionFile);
				project.setConfigs(configs);
				projects.add(project);
			}
		}
	}

	public void setName(String value)
	{
		name = value;
	}

	public String getName()
	{
		return name;
	}

	public void setProjects(List<Project> value)
	{
		projects = value;
	}

	public List<Project> getProjects()
	{
		return projects;
	}
	
	public void setBuildMachineConfiguration(String[] value)
	{
		buildMachineConfiguration = value;
	}

	public String[] getBuildMachineConfigurstion()
	{
		return buildMachineConfiguration;
	}
	
	public void setScripts(String[] value)
	{
		scripts = value;
	}

	public String[] getScripts()
	{
		return scripts;
	}
	
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if((obj == null) || !(obj instanceof Job))
			return false;
		Job other = (Job)obj;
		return (this.name.equals(other.getName()) &&
				Arrays.equals(this.scripts, other.getScripts()) &&
				Arrays.equals(this.buildMachineConfiguration, other.getBuildMachineConfigurstion()) &&
				this.projects.containsAll(other.getProjects()) &&
				other.getProjects().containsAll(this.projects));
	}
}