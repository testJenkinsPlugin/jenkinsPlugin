package com.amcbridge.buildserver.server;

import com.amcbridge.buildserver.builder.Builder;
import com.amcbridge.buildserver.builder.BuilderManager;
import com.amcbridge.jenkins.plugins.serialization.Config;
import com.amcbridge.jenkins.plugins.serialization.Job;
import com.amcbridge.jenkins.plugins.serialization.Project;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang3.StringUtils;
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

    private static final String QUOTE = "\"";

    private final BuildServer buildServer;
    private final BuilderManager builderManager;
    private Job job;

    private ProjectVersion ctrlVer;

    public JobManager(BuildServer buildServer) {
        this.buildServer = buildServer;
        this.builderManager = new BuilderManager();
    }

    public void init() throws Exception {
        loadJob();
        builderManager.init();
    }


    public void loadJob() throws Exception {
        DocumentBuilderFactory docBF = DocumentBuilderFactory.newInstance();
        DocumentBuilder bldDoc;
        Document docProject;

        bldDoc = docBF.newDocumentBuilder();
        File config = buildServer.getProperty(BuildServer.Builder.APP_PROPERTY_CONFIG);
        docProject = bldDoc.parse(config);
        Node jobNode = docProject.getDocumentElement();
        if (jobNode == null) {
            throw new Exception("Config file found, but getting job config failed");
        }


            if ( StringUtils.isNotEmpty(jobNode.getAttributes().getNamedItem(KEY_XML_NAME).getNodeValue())) {
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
        /*if (!project.getVersionFiles().isVersionFile() || project.getVersionFiles().getFiles().size() != 1) {
            ctrlVer =
            return false;
        }

        ctrlVer = new ProjectVersion(project.getRepository().getType(), *//*project.getRepository().getUrl(),*//*
                project.getVersionFiles().getFiles().get(0));
        return true;*/

        ctrlVer = new ProjectVersion(project);
    }

    public void setVersion(Project project) {
        if (!project.getVersionFiles().isVersionFile() || project.getVersionFiles().getFiles().size() != 1) {
            return;
        }

//        String file = project.getVersionFiles().getFiles().get(0);
//        ctrlVer.SetVersion(file, ctrlVer.GetVersionString(file));
    }

    public List<String> getConfigCommands(Project project) throws Exception {
        List<String> commandLines = new ArrayList<>();
        String command, attribute;
        Builder builder;

        for (Config config : project.getConfigs()) {
            if(config == null){
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
        return QUOTE + value + QUOTE;
    }

    public static String documentToXML(Node node) {
        String result = "";

        TransformerFactory transformerFactory;
        Transformer transformer;
        DOMSource source;
        Writer writer;
        StreamResult sr;

        try {
            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            source = new DOMSource(node);
            writer = new StringWriter();
            sr = new StreamResult(writer);
            transformer.transform(source, sr);
            result = writer.toString();
        } catch (Exception e) {
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
        String confValue = "";
        String regexpr = "\\[.*\\]";                      // Pattern string for "[...]"
        if ((confValue = config.getConfiguration()) != null) {                     // If checkgroup is checked ("Release", "Debug", "Other")
            if ("Other".equals(confValue)) {
                confValue = config.getUserConfig();
            }
            command = command.replace("config", confValue)                   // Replace "config" and "architecture" patterns by real values
                    .replace("architecture", config.getPlatform());
        } else {                                                                 // If no configuration is specified (e.g. checkgroup isn't checked)
            command = command.replaceFirst(regexpr, "");                         // Remove "[...]" from command (i.e. replace it by empty string)
        }
        command = command.replace("[", "").replace("]", "");                     // Remove all square brackets
        if (config.getBuilderArgs() != null && !config.getBuilderArgs().equals("")) {
            command = command + " " + config.getBuilderArgs();
        }
        return command;
    }

    protected String getFullProjectPath(Project project) {
        String result = buildServer.getCommandLine().getOptionValue(BuildServer.Builder.PARAM_WORKSPACE);// We start from Jenkins job's workspace
        String localDirectory = project.getLocalDirectory();
        if (localDirectory == null || localDirectory.isEmpty())
            result += File.separator +
                    project.getRepository().getUrl().substring(                  // Adding last URL's entry as local directory
                            project.getRepository().getUrl().lastIndexOf('/') + 1);
        //TODO: check when directory starts with ".", but contain more symbols, like : ".1", ".2" default directories
        else if (!localDirectory.equals("."))                                     // No need to add prefix for workspace direct checkout
            result += File.separator + localDirectory;                           // Adding base project folder if it's set
        return result;
    }
}
