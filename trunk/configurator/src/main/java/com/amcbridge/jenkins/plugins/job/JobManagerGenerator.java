package com.amcbridge.jenkins.plugins.job;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.configurationModels.BuilderConfigModel;
import com.amcbridge.jenkins.plugins.configurationModels.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.Configuration;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescriptionCheckBox;
import com.amcbridge.jenkins.plugins.job.SCM.JobGit;
import com.amcbridge.jenkins.plugins.job.SCM.JobNone;
import com.amcbridge.jenkins.plugins.job.SCM.JobSubversion;
import com.amcbridge.jenkins.plugins.serialization.*;
import com.thoughtworks.xstream.XStream;
import hudson.model.AbstractItem;
import hudson.model.Item;
import jenkins.model.Jenkins;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class JobManagerGenerator {

    private static final String BUILDSTEP_SHELL_SCRIPT_CLASS = "hudson.tasks.Shell";
    private static final String BUILDSTEP_BATCH_SCRIPT_CLASS = "hudson.tasks.BatchFile";
    public static final String CONFIG_BATCH_TYPE = "batch_type";
    public static final String CONFIG_SHELL_TYPE = "shell_type";
    private static final String PREBUILD_SCRIPT_POSITION = "preScript";
    private static final String POSTBUILD_SCRIPT_POSITION = "postScript";
    public static final String COMMA_SEPARATOR = ", ";
    private static final String JOB_TEMPLATE_PATH = "/plugins/build-configurator/job/config.xml";
    private static final String JOB_FOLDER_PATH = "/jobs/";
    private static final int[] SPECIAL_SYMBOLS = {40, 41, 43, 45, 95};
    private static final String XPATH_FILE_TO_COPY = "/project/buildWrappers/com.michelin.cio.hudson.plugins.copytoslave.CopyToSlaveBuildWrapper/includes/text()";
    private static final String XML_TITLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";


    public static String convertToXML(Object obj) {
        XStream xstream = new XStream();
        return xstream.toXML(obj);
    }

    public static void createJob(BuildConfigurationModel config)
            throws ParserConfigurationException,
            SAXException, IOException, TransformerException {
        String jobName = validJobName(config.getProjectName());

        List<String[]> prevArtefacts = new ArrayList<String[]>(config.getProjectToBuild().size());
        for (int i = 0; i < config.getProjectToBuild().size(); i++) {
            prevArtefacts.add(Arrays.copyOf(config.getProjectToBuild().get(i).getArtefacts(),
                    config.getProjectToBuild().get(i).getArtefacts().length));
        }
        correctArtifactPaths(config.getProjectToBuild());
        correctVersionFilesPaths(config.getProjectToBuild());
        correctLocalFolderPaths(config.getProjectToBuild());

        if (isJobExist(jobName)) {
            updateJobXML(jobName, config);
        } else {
            try {
                FileInputStream fis = new FileInputStream(getJobXML(config));
                Jenkins.getInstance().createProjectFromXML(jobName, fis);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                throw new IOException("Job not created!", ex);
            }
        }
        for (int i = 0; i < config.getProjectToBuild().size(); i++) {
            config.getProjectToBuild().get(i).setArtefacts(prevArtefacts.get(i));
        }
    }

    private static void correctArtifactPaths(List<ProjectToBuildModel> projectModels) {
        String pathPrefix;
        for (ProjectToBuildModel projectModel : projectModels) {
            pathPrefix = createPathPrefix(projectModel);
            String[] paths = new String[projectModel.getArtefacts().length];
            for (int i = 0; i < paths.length; i++) {
                String newPath = pathPrefix + projectModel.getArtefacts()[i].replaceAll("\\./", "");
                paths[i] = newPath;
            }
            projectModel.setArtefacts(paths);
        }
    }

    private static void correctVersionFilesPaths(List<ProjectToBuildModel> projectModels) {
        for (ProjectToBuildModel projectModel : projectModels) {
            String pathPrefix = createPathPrefix(projectModel);
            String[] paths = new String[projectModel.getVersionFiles().length];
            for (int i = 0; i < paths.length; i++) {
                String newPath = pathPrefix + projectModel.getVersionFiles()[i].replaceAll("\\./", "");
                paths[i] = newPath;
            }
            projectModel.setVersionFiles(paths);
        }
    }

    private static String createPathPrefix(ProjectToBuildModel projectModel) {
        String pathPrefix = "";
        if (projectModel.getLocalDirectoryPath() == null || projectModel.getLocalDirectoryPath().isEmpty()) {
            pathPrefix = projectModel.getProjectUrl().substring(projectModel.getProjectUrl().lastIndexOf('/') + 1);
        } else if (Pattern.matches("^\\.$|^(?:(?!\\.)[^\\\\/:*?\"<>|\\r\\n]+\\/?)*$", projectModel.getLocalDirectoryPath())) {
            if (!projectModel.getLocalDirectoryPath().equals(".")) // No need to add prefix for workspace direct checkout
            {
                pathPrefix = projectModel.getLocalDirectoryPath();
            }
        }
        if (!(pathPrefix.isEmpty() || pathPrefix.endsWith("/"))) {
            pathPrefix += "/";
        }
        return pathPrefix;
    }

    private static void correctLocalFolderPaths(List<ProjectToBuildModel> projectModels) {
        for (ProjectToBuildModel projectModel : projectModels) {
            String localDirectory = projectModel.getLocalDirectoryPath();
            String repoUrl = projectModel.getProjectUrl();
            if ((localDirectory == null || localDirectory.isEmpty())&&(repoUrl != null && !repoUrl.isEmpty())) {
                localDirectory =
                        repoUrl.substring(                  // Adding last URL's entry as local directory
                                repoUrl.lastIndexOf('/') + 1);
            }
            projectModel.setLocalDirectoryPath(localDirectory);
        }
    }

    public static Boolean isJobExist(String name) {
        for (Item item : Jenkins.getInstance().getAllItems()) {
            if (item.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static File getJobXML(BuildConfigurationModel config)
            throws ParserConfigurationException,
            SAXException, IOException, TransformerException {
        Document doc = loadTemplate(JOB_TEMPLATE_PATH);
        if (doc == null) {
            throw new FileNotFoundException(JOB_TEMPLATE_PATH + " file not found");
        }
        createJobConfigNodes(doc, config);
        createPreAndPostScriptsNodes(config, doc);
        setJobConfigFileName(doc, config.getProjectName());
        writeJobConfigForBuildServer(config);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        File file = BuildConfigurationManager.getFileToCreateJob();
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
        return file;
    }


    private static void updateJobXML(String jobName, BuildConfigurationModel config) throws IOException, TransformerException, SAXException, ParserConfigurationException {
        AbstractItem item = (AbstractItem) Jenkins.getInstance().getItemByFullName(jobName);
        String jobPath = JOB_FOLDER_PATH + jobName + "/config.xml";
        Document doc = loadTemplate(jobPath);
        if (doc == null) {
            throw new FileNotFoundException(JOB_TEMPLATE_PATH + " file not found");
        }

        createJobConfigNodes(doc, config);

        //removing pre and post scripts (first and last child nodes in <builders> node)
        Node buildStepNodeJ = doc.getElementsByTagName("builders").item(0);
        removeJunkElements(buildStepNodeJ.getChildNodes());
        Node scriptNode = buildStepNodeJ.getFirstChild();
        Node scriptChildNode = scriptNode.getFirstChild();
        if (config.getPreScript() != null) {
            removeScriptNode(scriptNode, scriptChildNode);
        }
        scriptNode = buildStepNodeJ.getLastChild();
        scriptChildNode = scriptNode.getLastChild();
        if (config.getPostScript() != null) {
            removeScriptNode(scriptNode, scriptChildNode);
        }
        createPreAndPostScriptsNodes(config, doc);
        setJobConfigFileName(doc, config.getProjectName());

        writeJobConfigForBuildServer(config);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        File file = BuildConfigurationManager.getFileToCreateJob();
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);

        Source streamSource = new StreamSource(file);
        item.updateByXml(streamSource);
        item.save();
    }

    private static void setJobConfigFileName(Document doc, String jobName) {

        Node fileNameNode = null;
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression exp;
        try {
            exp = xPath.compile(XPATH_FILE_TO_COPY);
            fileNameNode = (Node) exp.evaluate(doc, XPathConstants.NODE);
            fileNameNode.getTextContent();
            fileNameNode.getNodeValue();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        if (jobName != null && fileNameNode != null) {
            fileNameNode.setTextContent(jobName + ".xml");
        }

    }


    private static void writeJobConfigForBuildServer(BuildConfigurationModel config) throws IOException {
        Job job = buildJob(config);
        XStream xstream = new XStream();
        xstream.processAnnotations(Job.class);
        String paramsXML = xstream.toXML(job);
        String jobName = job.getName();

        String userContentPath = Jenkins.getInstance().getRootDir() + "/userContent/" + jobName + ".xml";

        FileOutputStream fos = new FileOutputStream(userContentPath);
        Writer out = new OutputStreamWriter(fos, BuildConfigurationManager.ENCODING);
        try {
            out.write(XML_TITLE);
            out.write(paramsXML);
        } finally {
            out.close();
            fos.close();
        }
    }


    private static void createJobConfigNodes(Document doc, BuildConfigurationModel config) throws IOException, SAXException, ParserConfigurationException {
        JobElementDescription jed;

        jed = new JobArtifacts();
        setElement(jed, doc, config);

        jed = new JobMailer();
        setElement(jed, doc, config);

        jed = new JobVersionFile();
        setElement(jed, doc, config);

        jed = new JobAssignedNode();
        setElement(jed, doc, config);

        jed = getSCM(config);
        setElement(jed, doc, config);

    }

    private static void createPreAndPostScriptsNodes(BuildConfigurationModel config, Document doc) {
        if (config.getScriptType() == null) {
            config.setScriptType(CONFIG_BATCH_TYPE);
        }

        String buildStepScriptClass = config.getScriptType().equals(CONFIG_BATCH_TYPE) ? BUILDSTEP_BATCH_SCRIPT_CLASS : BUILDSTEP_SHELL_SCRIPT_CLASS;

        NodeList buildStepNodeList = doc.getElementsByTagName("builders");
        Element buildStepNode;
        if (config.getPreScript() != null && !config.getPreScript().equals("")) {
            buildStepNode = doc.createElement(buildStepScriptClass);
            createScriptNode(doc, buildStepNode, config.getPreScript());
            buildStepNodeList.item(0).insertBefore(buildStepNode, buildStepNodeList.item(0).getFirstChild());
        }

        if (config.getPostScript() != null && !config.getPostScript().equals("")) {
            buildStepNode = doc.createElement(buildStepScriptClass);
            createScriptNode(doc, buildStepNode, config.getPostScript());
            buildStepNodeList.item(0).insertBefore(buildStepNode, null);
        }
    }

    private static void removeScriptNode(Node scriptNode, Node scriptChildNode) {

        if (scriptNode != null && (scriptNode.getNodeName().equals(BUILDSTEP_BATCH_SCRIPT_CLASS) || scriptNode.getNodeName().equals(BUILDSTEP_SHELL_SCRIPT_CLASS))) {
            if (scriptChildNode != null && scriptChildNode.getNodeName().equals("command")) {
                scriptNode.getParentNode().removeChild(scriptNode);
            }

        }
    }


    private static void createScriptNode(Document doc, Element buildStepNode, String scriptBody) {
        //Script  node changed for use same node name before and after update
        Element commandNode = doc.createElement("command");

        Text scriptBodyNode = doc.createTextNode(scriptBody);
        commandNode.appendChild(scriptBodyNode);
        buildStepNode.appendChild(commandNode);
    }

    private static JobElementDescription getSCM(BuildConfigurationModel config) {
        String scm = config.getScm();
        JobElementDescription jed;
        if (scm == null) {
            jed = new JobNone();
            return jed;
        }
        if (scm.equalsIgnoreCase("subversion")) {
            jed = new JobSubversion();
        } else if (scm.equalsIgnoreCase("git")) {
            jed = new JobGit();
        } else {
            jed = new JobNone();
        }
        return jed;
    }


    private static void removeJunkElements(NodeList builderList) {
        //removing empty text nodes like whitespaces, new line symbols etc.
        //going deep with recursion
        for (int i = 0; i < builderList.getLength(); i++) {
            removeJunkElements(builderList.item(i).getChildNodes());
        }
        List<Node> nodesToRemoveList = new LinkedList<>();
        for (int i = 0; i < builderList.getLength(); i++) {
            Node node = builderList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE && node.getNodeValue().trim().equals("")) {
                nodesToRemoveList.add(builderList.item(i));
            }
        }
        for (Node nodeToRemove : nodesToRemoveList) {
            nodeToRemove.getParentNode().removeChild(nodeToRemove);
        }
    }


    private static void setElement(JobElementDescription element, Document document, BuildConfigurationModel config)
            throws ParserConfigurationException, SAXException, IOException {
        if (!isNodeExist(document, element.getElementTag())) {
            if (!isNodeExist(document, element.getParentElementTag())) {
                Node mainNode = document.getFirstChild();
                mainNode.appendChild(document.createElement(element.getParentElementTag()));
            }
            Node elementNode = document.getElementsByTagName(element.getParentElementTag()).item(0);
            Node newNode = getNode(element.generateXML(config));

            if (element instanceof JobElementDescriptionCheckBox) {
                if (newNode.getFirstChild() == null) {
                    ((JobElementDescriptionCheckBox) element).uncheck(document);
                } else {
                    ((JobElementDescriptionCheckBox) element).check(document);
                }
            }

            if (newNode.getFirstChild() != null) {
                elementNode.appendChild(document.adoptNode(newNode.getChildNodes().item(0).cloneNode(true)));
            }
        } else {
            element.appendToXML(config, document);
        }
    }

    private static Boolean isNodeExist(Document document, String node) {
        return document.getElementsByTagName(node).getLength() > 0;
    }

    private static Node getNode(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        if (xml.isEmpty()) {
            return docBuilder.newDocument();
        }
        InputStream input = new ByteArrayInputStream(xml.getBytes());
        return docBuilder.parse(input);
    }

    public static Document loadTemplate(String path) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document doc = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(Jenkins.getInstance().getRootDir() + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public static String documentToXML(Node node) {
        String result = StringUtils.EMPTY;

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
            e.printStackTrace();
        }
        return result;
    }

    public static String validJobName(String name) {
        for (char ch : name.toCharArray()) {
            if (!Character.isLetterOrDigit(ch) && !ArrayUtils.contains(SPECIAL_SYMBOLS, ch)) {
                name = name.replace(ch, '_');
            }
        }
        return name;
    }

    public static void deleteJob(String name) throws IOException, InterruptedException {
        name = validJobName(name);
        for (Item job : Jenkins.getInstance().getAllItems()) {
            if (job.getName().equals(name)) {
                job.delete();
                return;
            }
        }
    }

    private static Job buildJob(BuildConfigurationModel config) {
        Job job = new Job();

        job.setName(JobManagerGenerator.validJobName(config.getProjectName()));
        job.setBuildMachineConfiguration(config.getBuildMachineConfiguration());
        job.setScripts(config.getScripts());

        if (config.getProjectToBuild() != null) {
            job.setProjects(new ArrayList<Project>(config.getProjectToBuild().size()));
            for (ProjectToBuildModel projectModel : config.getProjectToBuild()) {
                Project newProject = new Project();

                Repository repo = new Repository();
                repo.setType(config.getScm());
                repo.setUrl(projectModel.getProjectUrl());


                PathToArtefacts artifacts = new PathToArtefacts();
                for (String artifactPath : projectModel.getArtefacts()) {
                    artifacts.addFile(artifactPath);
                }

                VersionFile versionFiles = new VersionFile();
                for (String versionFilePath : projectModel.getVersionFiles()) {
                    versionFiles.addFile(versionFilePath);
                }
                if (!versionFiles.getFiles().isEmpty()) {
                    versionFiles.setIsVersionFile(true);
                }

                String localDirectory = projectModel.getLocalDirectoryPath().equals("") ? null : projectModel.getLocalDirectoryPath();
                List<Config> configurations = createJobConfigurations(projectModel);

                newProject.setRepository(repo);
                newProject.setPathToFile(projectModel.getFileToBuild());
                newProject.setLocalDirectory(localDirectory);
                newProject.setPathToArtefacts(artifacts);
                newProject.setVersionFiles(versionFiles);
                newProject.setConfigs(configurations);
                job.getProjects().add(newProject);
            }
        }
        return job;
    }

    private static List<Config> createJobConfigurations(ProjectToBuildModel projectModel) {
        List<Config> configurations = null;
        if (projectModel.getBuilders() != null) {
            configurations = new ArrayList<>(projectModel.getBuilders().length);
            for (BuilderConfigModel builderModel : projectModel.getBuilders()) {
                Config newConfig;
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
        return configurations;
    }
}
