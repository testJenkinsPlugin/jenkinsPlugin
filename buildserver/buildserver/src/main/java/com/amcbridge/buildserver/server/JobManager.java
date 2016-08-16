package com.amcbridge.buildserver.server;

import com.amcbridge.buildserver.builder.Builder;
import com.amcbridge.buildserver.builder.BuilderManager;
import com.amcbridge.jenkins.plugins.serialization.Config;
import com.amcbridge.jenkins.plugins.serialization.Job;
import com.amcbridge.jenkins.plugins.serialization.Project;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class JobManager {
    private static final String KEY_XML_NAME = "name";
    private final BuildServer buildServer;
    private final BuilderManager builderManager;
    private Job job;
    private static Logger logger = Logger.getLogger(JobManager.class);

    public JobManager(BuildServer buildServer) {
        this.buildServer = buildServer;
        this.builderManager = new BuilderManager();
    }

    public void init() throws Exception {
        loadJob();
        builderManager.init();
    }

    private void loadJob() throws Exception {
        DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
        DocumentBuilder bldDoc;
        Document docProject;

        bldDoc = docBF.newDocumentBuilder();
        File config = buildServer.getConfigFile();
        docProject = bldDoc.parse(config);
        Node jobNode = docProject.getDocumentElement();
        if (jobNode == null) {
            throw new Exception("Config file found, but getting job config failed");
        }
        if (StringUtils.isNotEmpty(jobNode.getAttributes().getNamedItem(KEY_XML_NAME).getNodeValue())) {
            XStream xstream = new XStream();
            xstream.processAnnotations(Job.class);
            String xml = documentToXML(jobNode);
            job = Job.class.cast(xstream.fromXML(xml));
        }

    }

    public Job getJob() {
        return job;
    }

    public void initVersion(Project project) {
        ProjectVersion version = new ProjectVersion();
        version.processVersions(project);
    }

    public List<String> getConfigCommands(Project project) throws Exception {
        List<String> commandLines = new ArrayList<>();
        String command;
        String attribute;
        Builder builder;

        for (Config config : project.getConfigs()) {
            if (config == null) {
                continue;
            }
            builder = builderManager.getBuilder(config.getBuilder());
            command = getConfiguredCommand(builder.getCommandLine(), config);
            attribute = project.getPathToFile();
            command = command.replace("solution", setQuotes(attribute));
            command = command.replace("executeBuild", setQuotes(builder.getExecuteBuild()));
            command = batchCommandProcessing(command, builder.getName());
            commandLines.add(command);
        }
        return commandLines;
    }

    private String setQuotes(String value) {
        return "\"" + value + "\"";
    }

    private static String documentToXML(Node node) {
        String result = "";
        TransformerFactory transformerFactory;
        Transformer transformer;
        DOMSource source;
        StreamResult sr;

        try (Writer writer = new StringWriter()) {
            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            source = new DOMSource(node);
            sr = new StreamResult(writer);
            transformer.transform(source, sr);
            result = writer.toString();
        } catch (Exception e) {
            logger.error("Error parsing xml file", e);
        }
        return result;
    }

    private String batchCommandProcessing(String command, String builder) {
        if (!builder.toLowerCase().contains("java"))
            return command;
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().contains("windows"))
            return "cmd /c " + command;
        else
            return command;
    }

    private String getConfiguredCommand(String command, Config config) {
        String confValue;
        String regexpr = "\\[.*\\]";
        if ((confValue = config.getConfiguration()) != null) {
            if ("Other".equals(confValue)) {
                confValue = config.getUserConfig();
            }
            command = command.replace("config", confValue)
                    .replace("architecture", config.getPlatform());
        } else {
            command = command.replaceFirst(regexpr, "");
        }
        command = command.replace("[", "").replace("]", "");
        if (config.getBuilderArgs() != null && !"".equals(config.getBuilderArgs())) {
            command = command + " " + config.getBuilderArgs();
        }
        return command;
    }

    protected String getFullProjectPath(Project project) {
        String result = buildServer.getCommandLine().getOptionValue(BuildServer.Builder.PARAM_WORKSPACE);
        String localDirectory = project.getLocalDirectory();
        if (localDirectory == null || localDirectory.isEmpty())
            result += File.separator +
                    project.getRepository().getUrl().substring(
                            project.getRepository().getUrl().lastIndexOf('/') + 1);
        else if (!".".equals(localDirectory))
            result += File.separator + localDirectory;
        return result;
    }
}
