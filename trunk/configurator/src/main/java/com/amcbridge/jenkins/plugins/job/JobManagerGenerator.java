package com.amcbridge.jenkins.plugins.job;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.configurationModels.BuilderConfigModel;
import com.amcbridge.jenkins.plugins.configurationModels.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.Configuration;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescriptionCheckBox;
import com.amcbridge.jenkins.plugins.job.SCM.JobGit;
import com.amcbridge.jenkins.plugins.job.SCM.JobNone;
import com.amcbridge.jenkins.plugins.job.SCM.JobSubversion;
import com.amcbridge.jenkins.plugins.serialization.*;
import com.amcbridge.jenkins.plugins.serialization.Job;
import com.amcbridge.jenkins.plugins.serialization.Project;
import com.amcbridge.jenkins.plugins.xmlSerialization.ExportSettings.Settings;
import com.thoughtworks.xstream.XStream;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class JobManagerGenerator {

    private static final String BUILDSTEP_SHELL_SCRIPT_CLASS = "hudson.tasks.Shell";
    private static final String BUILDSTEP_BATCH_SCRIPT_CLASS = "hudson.tasks.BatchFile";
    public static final String CONFIG_BATCH_TYPE = "batch_type";
    public static final String CONFIG_SHELL_TYPE = "shell_type";
    private static final String PREBUILD_SCRIPT_POSITION =  "preScript";
    private static final String POSTBUILD_SCRIPT_POSITION =  "postScript";
    public static final String COMMA_SEPARATOR = ", ";
    private static final String JOB_TEMPLATE_PATH = "\\plugins\\configurator\\job\\config.xml";
    private static final String JOB_FOLDER_PATH = "\\jobs\\";
    private static final int[] SPECIAL_SYMBOLS = {40, 41, 43, 45, 95};

    public static String convertToXML(Object obj) {
        XStream xstream = new XStream();
        return xstream.toXML(obj);
    }

    //TODO: refactor
    public static void createJob(BuildConfigurationModel config)
            throws FileNotFoundException, ParserConfigurationException,
            SAXException, IOException, TransformerException {
        String jobName = validJobName(config.getProjectName());

        List<String[]> prevArtefacts = new ArrayList<String[]>(config.getProjectToBuild().size());
        for (int i = 0; i < config.getProjectToBuild().size(); i++) {
            prevArtefacts.add(Arrays.copyOf(config.getProjectToBuild().get(i).getArtefacts(),
                    config.getProjectToBuild().get(i).getArtefacts().length));
        }
        JobManagerGenerator.correctArtifactPaths(config.getProjectToBuild());
        JobManagerGenerator.correctVersionFilesPaths(config.getProjectToBuild());

        if (isJobExist(jobName)) {
            updateJobXML(jobName, config);
        } else {
            try {
                FileInputStream fis = new FileInputStream(getJobXML(config, false));
                Jenkins.getInstance().createProjectFromXML(jobName, fis);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                throw new IOException("Job not created!", ex);
            }
        }

        for (int i = 0; i < config.getProjectToBuild().size(); i++) {
            config.getProjectToBuild().get(i).setArtefacts(prevArtefacts.get(i));
        }

        createConfigUpdaterJob();
        createConfigUpdaterJobOnSlaveNodes();

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

    //TODO: bug!
    private static void createConfigUpdaterJob() throws FileNotFoundException,
            ParserConfigurationException, SAXException, IOException, TransformerException {

        final String jobName = "BAMT_DEFAULT_CONFIG_UPDATER";
        if (isJobExist(jobName)) {
            return;
        }

        String url = "";
        Settings configSettings = new Settings();
        if (configSettings.isSettingsSet()) {
            url = configSettings.getUrl();
        }

        BuildConfigurationModel defaultJobModel = createBCModel(url);
        defaultJobModel.setProjectName(jobName);

        FileInputStream fis = new FileInputStream(getJobXML(defaultJobModel, true));
        Jenkins.getInstance().createProjectFromXML(jobName, fis);

    }

    private static void createConfigUpdaterJobOnSlaveNodes() throws FileNotFoundException,
            ParserConfigurationException, SAXException, IOException, TransformerException {

        final String jobName = "BAMT_DEFAULT_CONFIG_UPDATER";

        String url = "";
        Settings configSettings = new Settings();
        if (configSettings.isSettingsSet()) {
            url = configSettings.getUrl();
        }

        BuildConfigurationModel defaultJobModel = createBCModel(url);

        List<hudson.model.Node> slaveNodes = Jenkins.getInstance().getNodes();
        String[] nodeArray = new String[1];
        for (int i = 0; i < slaveNodes.size(); i++) {
            String newJobName = jobName + "_" + ((hudson.model.Node) slaveNodes.get(i)).getDisplayName();
            defaultJobModel.setProjectName(newJobName);
            nodeArray[0] = ((hudson.model.Node) slaveNodes.get(i)).getDisplayName();
            defaultJobModel.setBuildMachineConfiguration(nodeArray);
            FileInputStream fis = new FileInputStream(getJobXML(defaultJobModel, true));
            Jenkins.getInstance().createProjectFromXML(newJobName, fis);
        }
    }

    private static BuildConfigurationModel createBCModel(String url) {
        BuildConfigurationModel defaultJobModel = new BuildConfigurationModel();
        defaultJobModel.setEmail("");
        defaultJobModel.setCreator("");
        defaultJobModel.setCurrentDate();
        defaultJobModel.setJobUpdate(false);
        defaultJobModel.setRejectionReason("");
        Settings settings = new Settings();
        defaultJobModel.setScm(settings.getTypeSCM4Config());
        defaultJobModel.setScripts(null);
        defaultJobModel.setState(ConfigurationState.APPROVED);
        ProjectToBuildModel projectModel = new ProjectToBuildModel(
                url, "", "", "", "", "", ".", false, null);
        defaultJobModel.setProjectToBuild(Arrays.asList(projectModel));
        return defaultJobModel;
    }

    public static Boolean isJobExist(String name) {
        for (Item item : Jenkins.getInstance().getAllItems()) {
            if (item.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static File getJobXML(BuildConfigurationModel config, boolean removeAllBuilders)
            throws ParserConfigurationException,
            SAXException, IOException, TransformerException {
        Document doc = loadTemplate(JOB_TEMPLATE_PATH);
        if (doc == null) {
            throw new FileNotFoundException(JOB_TEMPLATE_PATH + " file not found");
        }

        JobElementDescription jed;

        jed = new JobArtefacts();
        setElement(jed, doc, config);

        jed = new JobMailer();
        setElement(jed, doc, config);

        jed = new JobVersionFile();
        setElement(jed, doc, config);

        jed = new JobAssigneNode();
        setElement(jed, doc, config);

        jed = getSCM(config);
        setElement(jed, doc, config);

        if (removeAllBuilders) {
            // for BAMT_DEFAULT_CONFIG_UPDATER job
            removeAllBuilders(doc);
        } else

        {
            createPreAndPostScriptsNodes(config, doc);
        }

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
        Document doc = loadTemplate(JOB_TEMPLATE_PATH);
        if (doc == null) {
            throw new FileNotFoundException(JOB_TEMPLATE_PATH + " file not found");
        }

        JobElementDescription jed;

        jed = new JobArtefacts();
        setElement(jed, doc, config);

        jed = new JobMailer();
        setElement(jed, doc, config);

        jed = new JobVersionFile();
        setElement(jed, doc, config);

        jed = new JobAssigneNode();
        setElement(jed, doc, config);

        jed = getSCM(config);
        setElement(jed, doc, config);


        removeAllBuilders(doc);

        String jobPath = JOB_FOLDER_PATH + jobName + "\\config.xml";
        Document docJob = loadTemplate(jobPath);
        if (docJob == null) {
            throw new FileNotFoundException(jobPath + " file not found");
        }


        //removing pre and post scripts (first and last child nodes in <builders> node)
        Node buildStepNodeJ = docJob.getElementsByTagName("builders").item(0);
        Node scriptNode = buildStepNodeJ.getFirstChild();
        removePreOrPostScriptNode(buildStepNodeJ, PREBUILD_SCRIPT_POSITION, scriptNode);
        scriptNode = buildStepNodeJ.getLastChild();
        removePreOrPostScriptNode(buildStepNodeJ, POSTBUILD_SCRIPT_POSITION, scriptNode);

        //importing <builders> node from job old job config to updated job
        Node noteToImport = docJob.getElementsByTagName("builders").item(0);
        Node importedNode = doc.importNode(noteToImport, true);
        doc.getElementsByTagName("project").item(0).appendChild(importedNode);

        createPreAndPostScriptsNodes(config, doc);

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

    private static void createPreAndPostScriptsNodes(BuildConfigurationModel config, Document doc) {
        if (config.getScriptType() == null) {
            config.setScriptType(CONFIG_BATCH_TYPE);
        }

        String buildStepScriptClass = config.getScriptType().equals(CONFIG_BATCH_TYPE) ? BUILDSTEP_BATCH_SCRIPT_CLASS : BUILDSTEP_SHELL_SCRIPT_CLASS;
        NodeList buildStepNodeList = doc.getElementsByTagName("builders");
        Element buildStepNode = null;
        //creating empty pre and post build scripts for correct job update
        if (config.getPreScript() == null) {
            config.setPreScript("");
        }
        buildStepNode = doc.createElement("buildStep");
        createScriptNode(doc, buildStepNode, PREBUILD_SCRIPT_POSITION, buildStepScriptClass, config.getPreScript());
        buildStepNodeList.item(0).insertBefore(buildStepNode, buildStepNodeList.item(0).getFirstChild());

        if (config.getPostScript() == null) {
            config.setPostScript("");
        }
        buildStepNode = doc.createElement("buildStep");
        createScriptNode(doc, buildStepNode, POSTBUILD_SCRIPT_POSITION, buildStepScriptClass, config.getPostScript());
        buildStepNodeList.item(0).insertBefore(buildStepNode, null /*buildStepNodeList.item(0).getLastChild()*/);

    }

    private static void removeAllBuilders(Document doc) {
        Node projectTagNode = doc.getElementsByTagName("project").item(0);
        for (int i = 0; i < projectTagNode.getChildNodes().getLength(); i++) {
            if (projectTagNode.getChildNodes().item(i).getNodeName().equals("builders")) {
                projectTagNode.removeChild(projectTagNode.getChildNodes().item(i));
                break;
            }
        }
    }

    private static void removePreOrPostScriptNode(Node scriptNodeParent, String nodeType, Node scriptNode) {

        while (scriptNode != null && scriptNode.getNodeType() != Node.ELEMENT_NODE) {
            scriptNode.getParentNode().removeChild(scriptNode);
            if (nodeType.equals(PREBUILD_SCRIPT_POSITION)) {
                scriptNode = scriptNodeParent.getFirstChild();
            } else {
                scriptNode = scriptNodeParent.getLastChild();
            }

        }

        if (scriptNode != null) {
            scriptNode.getParentNode().removeChild(scriptNode);
        }


    }


    private static void createScriptNode(Document doc, Element buildStepNode, String commandIdPosition, String buildStepScriptClass, String scriptBody) {


        buildStepNode.setAttribute("class", buildStepScriptClass);
        Element commandNode = doc.createElement("command");
        commandNode.setAttribute("id", commandIdPosition);

        Text scriptBodyNode = doc.createTextNode(scriptBody);
        commandNode.appendChild(scriptBodyNode);
        buildStepNode.appendChild(commandNode);

    }

    public static JobElementDescription getSCM(BuildConfigurationModel config) {
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
        if (document.getElementsByTagName(node).getLength() > 0) {
            return true;
        } else {
            return false;
        }
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

    //TODO: StringUtils
    public static String validJobName(String name) {
        for (char ch : name.toCharArray()) {
            if (!Character.isLetterOrDigit(ch) && !ArrayUtils.contains(SPECIAL_SYMBOLS, ch)) {
                name = name.replace(ch, ' ');
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

    //TODO: refactor
    public static Job buildJob(BuildConfigurationModel config) {
        Job job = new Job();

        job.setName(JobManagerGenerator.validJobName(config.getProjectName()));
        job.setBuildMachineConfiguration(config.getBuildMachineConfiguration());
        job.setScripts(config.getScripts());

        if (config.getProjectToBuild() != null) {
            job.setProjects(new ArrayList<Project>(config.getProjectToBuild().size()));
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
                job.getProjects().add(newProject);
            }
        }
        return job;
    }
}
