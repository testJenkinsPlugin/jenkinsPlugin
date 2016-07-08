package com.amcbridge.jenkins.plugins.job;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurator;
import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.models.BuilderConfigModel;
import com.amcbridge.jenkins.plugins.models.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.Configuration;
import com.amcbridge.jenkins.plugins.exceptions.JenkinsInstanceNotFoundException;
import com.amcbridge.jenkins.plugins.job.elementdescription.JobElementDescription;
import com.amcbridge.jenkins.plugins.job.elementdescription.JobElementDescriptionCheckBox;
import com.amcbridge.jenkins.plugins.job.scm.JobGit;
import com.amcbridge.jenkins.plugins.job.scm.JobNone;
import com.amcbridge.jenkins.plugins.job.scm.JobSubversion;
import com.amcbridge.jenkins.plugins.serialization.*;
import com.amcbridge.jenkins.plugins.serialization.Job;
import com.amcbridge.jenkins.plugins.serialization.Project;
import com.thoughtworks.xstream.XStream;
import hudson.model.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class JobManagerGenerator {

    public static final String COMMA_SEPARATOR = ", ";
    public static final String JOB_TEMPLATE_PATH = "/plugins/build-configurator/job/JobTemplate.xml";
    private static final String JOB_FOLDER_PATH = "/jobs/";
    private static final int[] SPECIAL_SYMBOLS = {40, 41, 43, 45, 95};
    private static final String XPATH_FILE_TO_COPY = "/project/buildWrappers/com.michelin.cio.hudson.plugins.copytoslave.CopyToSlaveBuildWrapper/includes/text()";
    private static final String XPATH_BUILDERS = "/project/builders/*";
    private static final String BATCH_COMMAND_TEXT = "java -jar \"%BUILDER_PATH%\\buildserver.jar\" -nodeName \"%NODE_NAME%\" -jobName \"%JOB_NAME%\" -workspace \"%WORKSPACE%\" -jenkinsHome \"%JENKINS_HOME%\"";
    private static final String BATCH_LABEL_TEXT = "${ENV,var=\"OS\"}";
    private static final String BATCH_EXPRESSION_TEXT = "(?i)Windows.*";
    private static final String SHELL_COMMAND_TEXT = "java -jar \"$BUILDER_PATH/buildserver.jar\" -nodeName \"$NODE_NAME\" -jobName \"$JOB_NAME\" -workspace \"$WORKSPACE\" -jenkinsHome $JENKINS_HOME";
    private static final String SHELL_LABEL_TEXT = "${ENV,var=\"OS\"}";
    private static final String SHELL_EXPRESSION_TEXT = "(?i)Windows.*";


    private static final Logger logger = LoggerFactory.getLogger(JobManagerGenerator.class);
    private static final String XML_TITLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private JobManagerGenerator(){}

    public static String convertToXML(Object obj) {
        XStream xstream = new XStream();
        return xstream.toXML(obj);
    }

    public static void createJob(BuildConfigurationModel config)
            throws ParserConfigurationException,
            SAXException, IOException, TransformerException, XPathExpressionException {
        String jobName = validJobName(config.getProjectName());

        List<String[]> prevArtefacts = new ArrayList<>(config.getProjectToBuild().size());
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
            File jobFile = createJobXMLFile(config, JOB_TEMPLATE_PATH, false);
            FileInputStream fis = new FileInputStream(jobFile);
            BuildConfigurationManager.getJenkins().createProjectFromXML(jobName, fis);

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

    public static Boolean isJobExist(String name) throws JenkinsInstanceNotFoundException {
        for (Item item : BuildConfigurationManager.getJenkins().getAllItems()) {
            if (item.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

  /*  private static File getJobXML(BuildConfigurationModel config)
            throws ParserConfigurationException,
            SAXException, IOException, TransformerException, XPathExpressionException {
        *//*Document doc = loadTemplate(JOB_TEMPLATE_PATH);
        if (doc == null) {
            throw new FileNotFoundException(JOB_TEMPLATE_PATH + " file not found");
        }
*//*

        return createJobXMLFile(config, JOB_TEMPLATE_PATH, false);
    }
*/

    private static void updateJobXML(String jobName, BuildConfigurationModel config) throws IOException, TransformerException, SAXException, ParserConfigurationException, XPathExpressionException {
        AbstractItem item = (AbstractItem) BuildConfigurationManager.getJenkins().getItemByFullName(jobName);
        if (item == null) {
            throw new NullPointerException("Jenkins item not found");
        }
        String jobPath = JOB_FOLDER_PATH + jobName + "/config.xml";

        File jobUpdateFile = createJobXMLFile(config, jobPath, true);
        Source streamSource = new StreamSource(jobUpdateFile);
        item.updateByXml(streamSource);
        item.save();
    }

    private static File createJobXMLFile(BuildConfigurationModel config, String pathToJob, boolean isFileForUpdate) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, TransformerException {
        Document doc = loadTemplate(pathToJob);
        boolean useBuildServer = false;
        if (doc == null) {
            throw new FileNotFoundException(pathToJob + " file not found");
        }
        WsPluginHelper.setWsPluginJobName(doc, config);

        for (ProjectToBuildModel project: config.getProjectToBuild()){
            if (project != null && project.getBuilders() != null && !project.getBuilders().isEmpty()){
                useBuildServer = true;
                break;
            }
        }

        if (!useBuildServer) {
            removeBuildersRunScript(doc);
        }
        if (!isFileForUpdate) {
            createJobConfigNodes(doc, config);

        } else {
            if (useBuildServer) {
                importScriptNode(loadTemplate(JOB_TEMPLATE_PATH), doc);
            }
        }
        setJobConfigFileName(doc, config.getProjectName());
        writeJobConfigForBuildServer(config);
        WsPluginHelper.wsPluginConfigure(doc,config);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        File file = BuildConfigurationManager.getFileToCreateJob();
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
        return file;
    }

    private static void importScriptNode(Document jobTemplate, Document copyToDoc) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {


        Node batchScriptNode = getBuildersScriptNode(jobTemplate, BATCH_EXPRESSION_TEXT, BATCH_LABEL_TEXT, BATCH_COMMAND_TEXT);
        Node shellScriptNode = getBuildersScriptNode(jobTemplate, SHELL_EXPRESSION_TEXT, SHELL_LABEL_TEXT, SHELL_COMMAND_TEXT);

        removeBuildersRunScript(copyToDoc);
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression exp;

        exp = xPath.compile("/project/builders[last()]");
        Node builderNode = (Node) exp.evaluate(copyToDoc, XPathConstants.NODE);
        batchScriptNode = copyToDoc.importNode(batchScriptNode, true);
        shellScriptNode = copyToDoc.importNode(shellScriptNode, true);

        builderNode.insertBefore(batchScriptNode,null);
        builderNode.insertBefore(shellScriptNode,null);


    }

    private static void removeBuildersRunScript(Document doc) throws XPathExpressionException {
        Node batchScriptNode = getBuildersScriptNode(doc, BATCH_EXPRESSION_TEXT, BATCH_LABEL_TEXT, BATCH_COMMAND_TEXT);
        Node shellScriptNode = getBuildersScriptNode(doc, SHELL_EXPRESSION_TEXT, SHELL_LABEL_TEXT, SHELL_COMMAND_TEXT);
        if (shellScriptNode != null) {
            shellScriptNode.getParentNode().removeChild(shellScriptNode);
        }
        if (batchScriptNode != null) {
            batchScriptNode.getParentNode().removeChild(batchScriptNode);
        }
    }

    private static NodeList getBuildersNodeList(Document doc) throws XPathExpressionException {
        NodeList buildersList;
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression exp;
        exp = xPath.compile(XPATH_BUILDERS);

        buildersList = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);

        return buildersList;
    }

    private static Node getBuildersScriptNode(Document doc, String expressionText, String labelText, String scriptText) throws XPathExpressionException {
        Node buildersScriptNode = null;
        NodeList buildersList;
        boolean expression = false;
        boolean label = false;
        boolean script = false;
        try {
            buildersList = getBuildersNodeList(doc);
        } catch (NullPointerException e) {
            return null;
        }
        for (int i = 0; i < buildersList.getLength(); i++) {

            //project/builders/org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder
            if (buildersList.item(i).getNodeName().equals("org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder")) {
                buildersScriptNode = buildersList.item(i);

                NodeList singleBuilderChildNodesList = buildersList.item(i).getChildNodes();
                //project/builders/org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder/buildStep
                for (int j = 0; j < singleBuilderChildNodesList.getLength(); j++) {
                    if (singleBuilderChildNodesList.item(j).getNodeName().equals("condition")) {
                        NodeList conditionsList = singleBuilderChildNodesList.item(j).getChildNodes();
                        for (int condition = 0; condition < conditionsList.getLength(); condition++) {
                            if (conditionsList.item(condition).getNodeName().equals("expression")) {
                                NodeList expressionList = conditionsList.item(condition).getChildNodes();
                                for (int expressionIndex = 0; expressionIndex < expressionList.getLength(); expressionIndex++) {
                                    if (expressionList.item(expressionIndex).getTextContent().equals(expressionText)) {
                                        expression = true;
                                    }
                                }
                            } else if (conditionsList.item(condition).getNodeName().equals("label")) {
                                NodeList labelList = conditionsList.item(condition).getChildNodes();
                                for (int labelIndex = 0; labelIndex < labelList.getLength(); labelIndex++) {
                                    if (labelList.item(labelIndex).getTextContent().equals(labelText)) {
                                        label = true;
                                    }
                                }
                            }
                        }
                    }

                    if (singleBuilderChildNodesList.item(j).getNodeName().equals("buildStep")) {
                        NodeList buildStepNodeList = singleBuilderChildNodesList.item(j).getChildNodes();
                        //project/builders/org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder/buildStep/command
                        for (int k = 0; k < buildStepNodeList.getLength(); k++) {
                            if (buildStepNodeList.item(k).getNodeName().equals("command") && buildStepNodeList.item(k).getTextContent().equals(scriptText)) {
                                script = true;
                            }
                        }
                    }
                }
            }
            if (expression && label && script) {
                return buildersScriptNode;
            }
        }
        return null;
    }

    private static void setJobConfigFileName(Document doc, String jobName) throws XPathExpressionException {

        Node fileNameNode;
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression exp;
            exp = xPath.compile(XPATH_FILE_TO_COPY);
            fileNameNode = (Node) exp.evaluate(doc, XPathConstants.NODE);
            fileNameNode.getTextContent();
            fileNameNode.getNodeValue();

        if (jobName != null) {
            fileNameNode.setTextContent(jobName + ".xml");
        }

    }

    private static void writeJobConfigForBuildServer(BuildConfigurationModel config) throws IOException {
        Job job = buildJob(config);
        XStream xstream = new XStream();
        xstream.processAnnotations(Job.class);
        String paramsXML = xstream.toXML(job);
        String jobName = job.getName();

        String userContentPath = BuildConfigurationManager.getJenkins().getRootDir() + "/userContent/" + jobName + ".xml";

        FileOutputStream fos = new FileOutputStream(userContentPath);
        Writer out = new OutputStreamWriter(fos, BuildConfigurationManager.ENCODING);
        out.write(XML_TITLE);
        out.write(paramsXML);
        out.close();
        fos.close();
    }


    private static void createJobConfigNodes(Document doc, BuildConfigurationModel config) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
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


    private static void setElement(JobElementDescription element, Document document, BuildConfigurationModel config)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        if (!isNodeExist(document, element.getElementTag())) {
            if (!isNodeExist(document, element.getParentElementTag())) {
                Node mainNode = document.getFirstChild();
                mainNode.appendChild(document.createElement(element.getParentElementTag()));
            }
            Node elementNode = document.getElementsByTagName(element.getParentElementTag()).item(0);
            Node newNode = getNode(element.generateXML(config));

            if (element instanceof JobElementDescriptionCheckBox) {
                if (newNode.getFirstChild() == null) {
                    ((JobElementDescriptionCheckBox) element).unCheck(document);
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

    public static Document loadTemplate(String path) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document doc;
        docBuilder = docFactory.newDocumentBuilder();
        doc = docBuilder.parse(BuildConfigurationManager.getJenkins().getRootDir() + path);
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
            logger.error("Error", e);
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
        for (Item job : BuildConfigurationManager.getJenkins().getAllItems()) {
            if (job.getName().equals( validJobName(name))) {
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
            configurations = new ArrayList<>(projectModel.getBuilders().size());
            for (BuilderConfigModel builderModel : projectModel.getBuilders()) {
                Config newConfig;
                if (builderModel.getConfigs().isEmpty()) {
                    newConfig = new Config();
                    newConfig.setBuilder(builderModel.getBuilder());
                    newConfig.setPlatform(builderModel.getPlatform());
                    if(builderModel.getBuilderArgs()!=null && !builderModel.getBuilderArgs().equals("")){
                        newConfig.setBuilderArgs(builderModel.getBuilderArgs());
                    }
                    configurations.add(newConfig);
                } else {
                    for (Configuration configEnum : builderModel.getConfigs()) {
                        newConfig = new Config(configEnum.toString(), builderModel.getBuilder(),
                                builderModel.getPlatform(), builderModel.getBuilderArgs());
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
