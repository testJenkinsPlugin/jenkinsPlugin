package com.amcbridge.jenkins.plugins.job;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.configurationModels.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescription;
import com.amcbridge.jenkins.plugins.job.ElementDescription.JobElementDescriptionCheckBox;
import com.amcbridge.jenkins.plugins.job.SCM.JobGit;
import com.amcbridge.jenkins.plugins.job.SCM.JobNone;
import com.amcbridge.jenkins.plugins.job.SCM.JobSubversion;
import com.amcbridge.jenkins.plugins.xmlSerialization.ExportSettings.Settings;
import com.thoughtworks.xstream.XStream;
import hudson.model.AbstractItem;
import hudson.model.Item;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jenkins.model.Jenkins;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class JobManagerGenerator {

    public static final String COMMA_SEPARATOR = ", ";

    private static final String JOB_TEMPLATE_PATH = "\\plugins\\configurator\\job\\config.xml";
    private static final int[] SPECIAL_SYMBOLS = {40, 41, 43, 45, 95};

    public static String convertToXML(Object obj) {
        XStream xstream = new XStream();
        return xstream.toXML(obj);
    }

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
        JobManagerGenerator.correctVersionFilePaths(config.getProjectToBuild());

        if (isJobExist(jobName)) {
            AbstractItem item = (AbstractItem) Jenkins.getInstance().getItemByFullName(jobName);
            Source streamSource = new StreamSource(getJobXML(config, false));
            item.updateByXml(streamSource);
            item.save();
        } else {
            try {
                FileInputStream fis = new FileInputStream(getJobXML(config, false));
                Jenkins.getInstance().createProjectFromXML(jobName, fis);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
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
            pathPrefix = "";
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
            String[] newArtefactsPaths = new String[projectModel.getArtefacts().length];
            int counter = 0;
            for (String artefactPath : projectModel.getArtefacts()) {
                newArtefactsPaths[counter++] = pathPrefix + artefactPath.replaceAll("\\./", "");
            }
            projectModel.setArtefacts(newArtefactsPaths);
        }
    }
    
    private static void correctVersionFilePaths(List<ProjectToBuildModel> projectModels) {
        String pathPrefix;
        for (ProjectToBuildModel projectModel : projectModels) {
            pathPrefix = "";
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
            String[] newVersionFilePath = new String[projectModel.getVersionFiles().length];
            int counter = 0;
            for (String versionFilePath : projectModel.getVersionFiles()) {
                newVersionFilePath[counter++] = pathPrefix + versionFilePath.replaceAll("\\./", "");
            }
            projectModel.setVersionFiles(newVersionFilePath);
        }
    }

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
        try{
            Jenkins.getInstance().createProjectFromXML(jobName, fis);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        
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
//        String[] nodeArray = new String[slaveNodes.size()];
        String[] nodeArray = new String[1];
        for(int i = 0; i<slaveNodes.size(); i++){
//            nodeArray[i] = ((hudson.model.Node)slaveNodes.get(i)).getDisplayName();
            String newJobName = jobName+ "_" + ((hudson.model.Node)slaveNodes.get(i)).getDisplayName();
            defaultJobModel.setProjectName(newJobName);
            nodeArray[0] = ((hudson.model.Node)slaveNodes.get(i)).getDisplayName();
            defaultJobModel.setBuildMachineConfiguration(nodeArray);
            FileInputStream fis = new FileInputStream(getJobXML(defaultJobModel, true));
            Jenkins.getInstance().createProjectFromXML(newJobName, fis);
        }
    }

    private static BuildConfigurationModel createBCModel(String url){
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
                url, "","", "", "", ".", false, null);
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
            return null;
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
            Node projectTagNode = doc.getElementsByTagName("project").item(0);
            for (int i = 0; i < projectTagNode.getChildNodes().getLength(); i++) {
                if (projectTagNode.getChildNodes().item(i).getNodeName().equals("builders")) {
                    projectTagNode.removeChild(projectTagNode.getChildNodes().item(i));
                    break;
                }
            }
        }

        
        Node buildersTagNode = doc.getElementsByTagName("builders").item(0);
        for (int i = 0; i < buildersTagNode.getChildNodes().getLength(); i++) {
            if (buildersTagNode.getChildNodes().item(i).getNodeName().equals("buildStep")) {
                Node buildStepTagNode = buildersTagNode.getChildNodes().item(i);

                for (int j = 0; j < buildStepTagNode.getChildNodes().getLength(); j++) {
                    if (buildStepTagNode.getChildNodes().item(j).getNodeName().equals("command")) {
                        Node commandPreOrPost = buildStepTagNode.getChildNodes().item(j);
                        if (commandPreOrPost.getAttributes().getNamedItem("id") != null) {
                            String id = commandPreOrPost.getAttributes().getNamedItem("id").getNodeValue();
                            if (id.equalsIgnoreCase("preScript")){
                                if (config.getPreScript() != null) {
                                    commandPreOrPost.setTextContent(config.getPreScript());
                                } else {
                                    commandPreOrPost.setTextContent("pre empty");
                                }
                            } else if (id.equalsIgnoreCase("postScript")){
                                if (config.getPostScript()!= null) {
                                    commandPreOrPost.setTextContent(config.getPostScript());
                                } else {
                                    commandPreOrPost.setTextContent("post empty");
                                }
                                
                            }
                            
                        }
                    }
                }

            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        File file = BuildConfigurationManager.getFileToCreateJob();
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);

        return file;
    }

    public static JobElementDescription getSCM(BuildConfigurationModel config) {
        String scm = config.getScm();
        JobElementDescription jed;
        if (scm == null) {
            jed = new JobNone();
            return jed;
        }
        if (scm.equalsIgnoreCase("subversion")){
            jed = new JobSubversion();
        } else if (scm.equalsIgnoreCase("git")){
            jed = new JobGit(config.getProjectRemoteUrl(),config.getBranchName());
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
}
