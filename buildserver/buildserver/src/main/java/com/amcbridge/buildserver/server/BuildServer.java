package com.amcbridge.buildserver.server;

import com.amcbridge.jenkins.plugins.serialization.Job;
import com.amcbridge.jenkins.plugins.serialization.Project;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;

public class BuildServer {
    private static final String NODE_NAME_MASTER = "master";
    private CommandLine commandLine;
    private JobManager jobManager;
    private static Logger logger = Logger.getLogger(BuildServer.class);
    private File configFile;

    private BuildServer() {
    }

    private void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    private void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public void init() throws Exception {
        configFile = loadConfigurationFile();
        jobManager.init();
    }

    private File loadConfigurationFile() {
        String nodeName = commandLine.getOptionValue(Builder.PARAM_NODE_NAME);
        String jobName = commandLine.getOptionValue(Builder.PARAM_JOB_NAME);
        String jenkinsHomePath = commandLine.getOptionValue(Builder.PARAM_JENKINS_HOME);
        String workSpace = commandLine.getOptionValue(Builder.PARAM_WORKSPACE);
        if (nodeName == null || jenkinsHomePath == null || workSpace == null) {
            throw new IllegalArgumentException("Missed or wrong arguments. Check build or job file configurations(stored on Jenkins Master)");
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
            if (project == null || project.getConfigs() == null) {
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
            logger.info(String.format("COMMAND:%n%s%n%n", command));
            File fullProjectPath = new File(jobManager.getFullProjectPath(project));
            Process process = Runtime.getRuntime().exec(command, null, fullProjectPath);
            processOutput(process.getInputStream(), "RESULT");
            if (processOutput(process.getErrorStream(), "ERROR STREAM") == 0) {
                System.exit(1);
            }
            int returnCode = process.waitFor();
            logger.info(String.format("%s:\t%s%n%n", "RETURN CODE", returnCode));
            if (returnCode != 0) {
                System.exit(returnCode);
            }
        } catch (Exception ex) {
            logger.error("Executing project's command error: ", ex);
            System.exit(1);
        }
    }

    private int processOutput(InputStream stream, String message) {
        boolean msgWritten = false;
        String line;
        try (BufferedReader stdStream = new BufferedReader(new InputStreamReader(stream))) {
            while ((line = stdStream.readLine()) != null) {
                if (!msgWritten) {
                    logger.info(String.format("%s:", message));
                    msgWritten = true;
                }
                logger.info(String.format("%n%s", line));
            }
            logger.info(String.format("%n"));
        } catch (IOException ex) {
            logger.error("Error while reading process' stream: ", ex);
            System.exit(1);
        }
        return msgWritten ? 0 : 1;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public File getConfigFile() {
        return configFile;
    }

    public static class Builder {
        public static final String PARAM_NODE_NAME = "nodeName";
        public static final String PARAM_JOB_NAME = "jobName";
        public static final String PARAM_WORKSPACE = "workspace";
        public static final String PARAM_JENKINS_HOME = "jenkinsHome";
        public static final String KEY_ENV_INS_PATH = "BUILDER_PATH";
        private BuildServer buildServer;
        private CommandLine commandLine;
        private String[] args;
        private Options options;
        private JobManager jobManager;

        private Builder() {
            this.buildServer = new BuildServer();
            this.defaultOptions();
            this.defaultJobManager();
        }

        public void defaultOptions() {
            this.options = new Options();
            options.addOption(PARAM_NODE_NAME, true, "some description");
            options.addOption(PARAM_JOB_NAME, true, "some description");
            options.addOption(PARAM_WORKSPACE, true, "some description");
            options.addOption(PARAM_JENKINS_HOME, true, "some description");
        }

        public void defaultJobManager() {
            this.jobManager = new JobManager(this.buildServer);
        }

        public Builder setArgs(String... args) {
            if (args != null) {
                this.args = args;
            }
            return this;
        }

        private void initCommandLine() throws BuildException {
            CommandLineParser parser = new GnuParser();
            try {
                this.commandLine = parser.parse(options, args);
            } catch (ParseException e) {
                throw new BuildException("Command line parsing error: " + e.getMessage());
            }
        }

        public BuildServer build() throws Exception {
            this.initCommandLine();
            this.buildServer.setCommandLine(this.commandLine);
            this.buildServer.setJobManager(this.jobManager);
            return this.buildServer;
        }
    }
}
