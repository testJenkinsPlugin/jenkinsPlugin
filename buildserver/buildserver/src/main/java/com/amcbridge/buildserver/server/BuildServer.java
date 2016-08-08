package com.amcbridge.buildserver.server;

import com.amcbridge.jenkins.plugins.serialization.Job;
import com.amcbridge.jenkins.plugins.serialization.Project;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildServer {

    public static final String NODE_NAME_MASTER = "master";
    private CommandLine commandLine;
    private Map<String, Object> propertyMap;
    private JobManager jobManager;

    private BuildServer() {
    }

    private void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    private void setPropertyMap(Map<String, Object> propertyMap) {
        this.propertyMap = propertyMap;
    }

    private void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public void init() throws Exception {
        this.loadConfig();
        this.jobManager.init();
    }

    private void loadConfig() throws IOException {
        File configFile = getConfigFile();
        this.propertyMap.put(Builder.APP_PROPERTY_CONFIG, configFile);
    }


    private File getConfigFile() {
        String nodeName = commandLine.getOptionValue(Builder.PARAM_NODE_NAME);
        String jobName = commandLine.getOptionValue(Builder.PARAM_JOB_NAME);
        String jenkinsHomePath = commandLine.getOptionValue(Builder.PARAM_JENKINS_HOME);
        String workSpace = commandLine.getOptionValue(Builder.PARAM_WORKSPACE);
        if (nodeName == null || jenkinsHomePath == null || workSpace == null) {
            throw new  IllegalArgumentException("Missed or wrong arguments. Check build or job file configurations(stored on Jenkins Master)");
        }
        String filePath;
        if (nodeName.equals(NODE_NAME_MASTER)) {
            filePath = jenkinsHomePath + "/userContent/" + jobName + ".xml";
        } else {
            filePath = workSpace + "/" + jobName + ".xml";
        }

        return new File(filePath);

    }


    public void execute() throws Exception {
        Job job = jobManager.getJob();

        for (Project project : job.getProjects()) {
            if(project == null || project.getConfigs() == null){
                continue;
            }
            jobManager.initVersion(project);
            List<String> commands = jobManager.getConfigCommands(project);
            for (String command : commands) {
                executeProcess(jobManager, project, command);
            }
        }
    }

    private void executeProcess(JobManager jobManager, Project project, String command) {
        try {
            System.out.print(String.format("COMMAND:%n%s%n%n", command));
            File fullProjectPath = new File(jobManager.getFullProjectPath(project));

            Process process = Runtime.getRuntime().exec(command, null, fullProjectPath);
            processOutput(process.getInputStream(), "RESULT");
            if (processOutput(process.getErrorStream(), "ERROR STREAM") == 0)           // We have written error message
                System.exit(1);

            int returnCode = process.waitFor();
            System.out.print(String.format("%s:\t%s%n%n", "RETURN CODE", returnCode));
            if (returnCode == 0) {
//                jobManager.setVersion(project);
            } else {
                System.exit(returnCode);
            }
        } catch (Exception ex) {
            Logger.getLogger(BuildServer.class.getName()).log(Level.SEVERE, "Executing project's command error: ", ex);
            System.exit(1);
        }
    }

    private int processOutput(InputStream stream, String message) {
        BufferedReader stdStream = new BufferedReader(new InputStreamReader(stream));
        boolean msgWritten = false;
        String line = "";
        try {
            while ((line = stdStream.readLine()) != null) {
                if (!msgWritten) {
                    System.out.print(String.format("%s:", message));
                    msgWritten = true;
                }
                System.out.print(String.format("%n%s", line));
            }
            stdStream.close();
            System.out.print(String.format("%n"));
        } catch (IOException ex) {
            Logger.getLogger(BuildServer.class.getName()).log(Level.SEVERE, "Error while reading process' stream: ", ex);
            System.exit(1);
        }
        return (msgWritten) ? 0 : 1;
    }

    public <T extends Object> T getProperty(String key) {
        return (T) this.propertyMap.getOrDefault(key, null);
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {


        public static final String PARAM_NODE_NAME = "nodeName";
        public static final String PARAM_JOB_NAME = "jobName";
        public static final String PARAM_WORKSPACE = "workspace";
        public static final String PARAM_JENKINS_HOME = "jenkinsHome";
        public static final String APP_PROPERTY_CONFIG = "app.config";
        public static final String KEY_ENV_INS_PATH = "BUILDER_PATH";

        private BuildServer buildServer;

        private CommandLine commandLine;
        private Map<String, Object> propertyMap;
        private String[] args;
        private Options options;
        private JobManager jobManager;

        private Builder() {
            this.buildServer = new BuildServer();
            this.defaultOptions()
                    .defaultProperty()
                    .defaultJobManager();
        }

        public Builder defaultOptions() {
            this.options = new Options();

            options.addOption(PARAM_NODE_NAME,true,"some description");
            options.addOption(PARAM_JOB_NAME,true,"some description");
            options.addOption(PARAM_WORKSPACE,true,"some description");
            options.addOption(PARAM_JENKINS_HOME,true,"some description");



            return this;
        }

        public Builder defaultProperty() {
            this.propertyMap = new HashMap<>();
            return this;
        }

        public Map<String, Object> getProperty() {
            return this.propertyMap;
        }

        public Builder defaultJobManager() {
            this.jobManager = new JobManager(this.buildServer);
            return this;
        }

        public Builder setArgs(String... args) {
            if (args != null) {
                this.args = args;
            }
            return this;
        }

        private Builder initCommandLine() throws BuildException {
            CommandLineParser parser = new GnuParser();
            try {
                this.commandLine = parser.parse(options, args);
            } catch (ParseException e) {
                throw new BuildException("Command line parsing error: " + e.getMessage());
            }
            return this;
        }

        public BuildServer build() throws Exception {
            this.initCommandLine();
            this.buildServer.setCommandLine(this.commandLine);
            this.buildServer.setJobManager(this.jobManager);
            this.buildServer.setPropertyMap(this.propertyMap);
            return this.buildServer;
        }
    }
}
