package com.amcbridge.jenkins.plugins.xmlSerialization;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.serialization.Job;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("configurations")
public class XmlExporter {

    public static final String XML_TITLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private List<Job> configurations;

    public List<Job> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Job> value) {
        configurations = value;
    }

    public XmlExporter() {
        configurations = new ArrayList<Job>();
    }

    public static List<Job> generateConfiguration() throws IOException {
        List<Job> jobs = new ArrayList<Job>();
        Job job;
        BuildConfigurationModel config = null;

        File file = new File(BuildConfigurationManager.getRootDirectory());

        if (!file.exists()) {
            return jobs;
        }

        File[] directories = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        for (int i = 0; i < directories.length; i++) {
            config = BuildConfigurationManager.load(directories[i].getName());
            if (config.getState().equals(ConfigurationState.APPROVED)) {
                job = JobManagerGenerator.buildJob(config);
                jobs.add(job);
                BuildConfigurationManager.acm.add(job);
                continue;
            }
            if (!config.getState().equals(ConfigurationState.NEW)) {
                job = BuildConfigurationManager.acm.get(config.getProjectName());
                if (job != null) {
                    jobs.add(job);
                }
            }
        }
        return jobs;
    }

    public String exportToXml(Boolean forVSC) throws IOException {
        configurations = generateConfiguration();
        return saveConfigurations(forVSC);
    }

    private String saveConfigurations(Boolean forVSC) throws IOException {
        String path;
        XStream xstream = new XStream();
        xstream.processAnnotations(XmlExporter.class);
        xstream.addImplicitCollection(XmlExporter.class, "configurations");
        File outputFile;
        if (forVSC) {
            outputFile = BuildConfigurationManager.getFileToExportConfigurations();
        } else {
            outputFile = BuildConfigurationManager.getFileForCheckVSC();
        }

        File rootDirectory = new File(BuildConfigurationManager.getRootDirectory());
        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
        }

        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        path = outputFile.getPath();
        FileOutputStream fos = new FileOutputStream(path);
        Writer out = new OutputStreamWriter(fos, BuildConfigurationManager.ENCODING);
        try {
            out.write(XML_TITLE);
            out.write(xstream.toXML(this));
        } finally {
            out.close();
            fos.close();
        }
        return path;
    }
}
