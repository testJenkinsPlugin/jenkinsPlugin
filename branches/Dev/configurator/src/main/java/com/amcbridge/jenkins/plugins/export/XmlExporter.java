package com.amcbridge.jenkins.plugins.export;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

import com.amcbridge.jenkins.plugins.configuration.BuildConfiguration;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.configurator.ConfigurationState;
import com.amcbridge.jenkins.plugins.controls.Configuration;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("configurations")
public class XmlExporter
{
	private static final String XML_TITLE = "<?xml version='1.0' encoding='UTF-8'?>\n";

	private List<Job> configurations;

	public List<Job> getConfigurations()
	{
		return configurations;
	}

	public void setConfigurations(List<Job> value)
	{
		configurations = value;
	}

	public XmlExporter()
	{
		configurations = new ArrayList<Job>();
	}

	public String exportToXml() throws IOException
	{
		List<Job> jobs = new ArrayList<Job>();

		Job job;
		Repository repo;
		Project project;
		PathToArtefacts artefacts;
		VersionFile versionFile;
		Config conf;
		List<Config> configs;
		List<Project> projects;
		BuildConfiguration config = null;

		File file = new File(BuildConfigurationManager.getRootDirectory());

		if (!file.exists())
			return BuildConfigurationManager.STRING_EMPTY;

		File[] directories = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		for (int i = 0; i < directories.length; i++)
		{
			config = BuildConfigurationManager.load(directories[i].getName());
			if (config.getState().equals(ConfigurationState.APPROVED))
			{
				job = new Job(config.getProjectName());
				job.setBuildMachineConfiguration(config.getBuildMachineConfiguration());
				job.setScripts(config.getScripts());
				if (config.getProjectToBuild() != null)
				{
					projects = new ArrayList<Project>();
					for (int j=0; j<config.getProjectToBuild().size(); j++)
					{
						repo = new Repository();
						project = new Project();

						repo.setType(config.getProjectToBuild().get(j).getType());
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
					job.setProjects(projects);
					jobs.add(job);
				}
			}
		}
		configurations = jobs;
		return saveConfigurations();
	}

	private String saveConfigurations() throws IOException
	{
		String path;
		XStream xstream = new XStream();
		xstream.processAnnotations(XmlExporter.class);
		xstream.addImplicitCollection(XmlExporter.class, "configurations");
		File outputFile = BuildConfigurationManager.getFileToExportConfigurations();
		if (!outputFile.exists())
			outputFile.createNewFile();
		path = outputFile.getPath();
		FileOutputStream fos = new FileOutputStream(path);
		try
		{
			fos.write(XML_TITLE.getBytes());
			fos.write(xstream.toXML(this).getBytes());
		}
		finally
		{
			fos.close();
		}
		return path;
	}
}
