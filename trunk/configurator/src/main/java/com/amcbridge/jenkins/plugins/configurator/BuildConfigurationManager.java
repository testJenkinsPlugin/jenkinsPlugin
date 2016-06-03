package com.amcbridge.jenkins.plugins.configurator;

import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.models.UserAccessModel;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import com.amcbridge.jenkins.plugins.enums.MessageDescription;
import com.amcbridge.jenkins.plugins.exceptions.JenkinsInstanceNotFoundException;
import com.amcbridge.jenkins.plugins.xstreamelements.SCM;
import com.amcbridge.jenkins.plugins.xstreamelements.SCMLoader;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.messenger.ConfigurationStatusMessage;
import com.amcbridge.jenkins.plugins.messenger.MailSender;
import com.amcbridge.jenkins.plugins.serialization.CredentialItem;
import hudson.XmlFile;
import hudson.model.Node;
import hudson.model.User;
import hudson.scm.SCMDescriptor;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.util.Iterators;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class BuildConfigurationManager {

    public static final String CONFIG_FILE_NAME = "config.xml";
    public static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final String ENCODING = "UTF-8";
    private static final String CONFIG_JOB_FILE_NAME = "JobConfig.xml";
    private static final String BUILD_CONFIGURATOR_DIRECTORY_NAME = "/plugins/BuildConfiguration";
    private static final String CONTENT_FOLDER = "userContent";
    public static final String STRING_EMPTY = "";
    private static final MailSender mail = new MailSender();
    private static final Logger log = LoggerFactory.getLogger(BuildConfigurationManager.class);
    private static SCMLoader scmLoader;
    private static String defaultCredentialsPropertiesFileName = "credentialsDefaults.properties";
    private static String credentialsPropertyName = "defaultCredentials";

    public static String getCurrentUserID() {
        User user = User.current();
        if (user != null) {
            return user.getId();
        } else {
            return STRING_EMPTY;
        }
    }

    public static File getConfigFileFor(String id) throws JenkinsInstanceNotFoundException {
        return new File(new File(getRootDir(), id), CONFIG_FILE_NAME);
    }


    public static File getFileToCreateJob() throws JenkinsInstanceNotFoundException {
        return new File(getRootDir() + "/" + CONFIG_JOB_FILE_NAME);
    }

    static File getRootDir() throws JenkinsInstanceNotFoundException {
        return new File(BuildConfigurationManager.getJenkins().getRootDir(),
                BUILD_CONFIGURATOR_DIRECTORY_NAME);
    }

    public static String getRootDirectory() throws JenkinsInstanceNotFoundException {
        return BuildConfigurationManager.getJenkins().getRootDir() + "/"
                + BUILD_CONFIGURATOR_DIRECTORY_NAME;
    }

    public static String getUserContentFolder() throws JenkinsInstanceNotFoundException {
        return BuildConfigurationManager.getJenkins().getRootDir() + "/" + CONTENT_FOLDER;
    }

    public static void save(BuildConfigurationModel config) throws IOException,
            ParserConfigurationException, JAXBException {
        if (config.getProjectName().isEmpty()) {
            deleteFiles(config.getScripts(), getUserContentFolder());
            return;
        }

        File checkFile = new File(getRootDirectory() + "/" + config.getProjectName());
        if (!checkFile.exists()) {
            checkFile.mkdirs();
        }

        XmlFile fileWriter = getConfigFile(config.getProjectName());
        fileWriter.write(config);
    }


    protected final static XmlFile getConfigFile(String nameProject) throws JenkinsInstanceNotFoundException {
        return new XmlFile(Jenkins.XSTREAM, getConfigFileFor("/" + nameProject));
    }

    public static BuildConfigurationModel load(String nameProject) throws IOException {
        BuildConfigurationModel result = new BuildConfigurationModel();
        XmlFile config = getConfigFile(nameProject);

        if (config.exists()) {
            config.unmarshal(result);
        }
        return result;
    }

    public static List<BuildConfigurationModel> loadAllConfigurations()
            throws IOException, ServletException, JAXBException {
        List<BuildConfigurationModel> configs = new ArrayList<>();
        File file = new File(getRootDirectory());

        if (!file.exists()) {
            return new LinkedList<BuildConfigurationModel>();
        }

        File[] directories = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        for (File directory : directories) {
            if (!isCurrentUserHasAccess(directory.getName())) {
                continue;
            }
            configs.add(load(directory.getName()));
        }
        return configs;
    }

    public static void deleteFiles(String[] files, String pathFolder) {
        File file;
        for (String strFile : files) {
            if (strFile.isEmpty()) {
                continue;
            }
            file = new File(pathFolder + "/" + strFile);
            file.delete();
        }
    }

    public static void markConfigurationForDeletion(String name)
            throws IOException, ParserConfigurationException,
            JAXBException, MessagingException {
        BuildConfigurationModel config = load(name);
        if (config.getState() == ConfigurationState.FOR_DELETION) {
            return;
        }
        config.setState(ConfigurationState.FOR_DELETION);
        config.initCurrentDate();
        save(config);
        String userEmail = StringUtils.EMPTY;
        if (!getUserMailAddress(config).isEmpty()) {
            userEmail = getUserMailAddress(config);
        }
        ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName(),
                getAdminEmail(), userEmail, MessageDescription.MARKED_FOR_DELETION.toString(),
                config.getProjectName());
        mail.sendMail(message);
    }

    public static void restoreConfiguration(String name) throws IOException, ParserConfigurationException,
            JAXBException, MessagingException {
        BuildConfigurationModel config = load(name);
        config.setState(ConfigurationState.UPDATED);
        save(config);
        String userEmail = StringUtils.EMPTY;
        if (!getUserMailAddress(config).isEmpty()) {
            userEmail = getUserMailAddress(config);
        }
        ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName(),
                getAdminEmail(), userEmail, MessageDescription.RESTORE.toString(),
                config.getProjectName());
        mail.sendMail(message);
    }


    public static Boolean isNameUsing(String name) throws JenkinsInstanceNotFoundException {
        File checkName = new File(getRootDirectory() + "/" + name);
        return checkName.exists();
    }

    public static void deleteConfigurationPermanently(String name) throws IOException,
            MessagingException, JAXBException, InterruptedException, ParserConfigurationException {
        File checkFile = new File(getRootDirectory() + "/" + name);
        BuildConfigurationModel config = load(name);
        if (checkFile.exists()) {
            FileUtils.deleteDirectory(checkFile);
        }

        deleteJob(name);

        ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName());
        message.setSubject(config.getProjectName());
        message.setDescription(MessageDescription.DELETE_PERMANENTLY.toString());
        if (!getUserMailAddress(config).isEmpty()) {
            message.setCC(BuildConfigurationManager.getUserMailAddress(config));
        }
        message.setDestinationAddress(getAdminEmail());
        mail.sendMail(message);
    }

    public static Boolean isCurrentUserHasAccess(String name) throws IOException {
        boolean isUserAdmin = isCurrentUserAdministrator();
        boolean isUserInAccessList = false;

        if (isUserAdmin) {
            return true;
        }
        BuildConfigurationModel configs = load(name);
        List<UserAccessModel> usersList = configs.getUserWithAccess();
        if (usersList != null) {
            isUserInAccessList = configs.getUserWithAccess().contains(new UserAccessModel(getCurrentUserID()));
        }
        boolean isCurrentUserCreator = configs.getCreator().equals(getCurrentUserID());
        return isUserInAccessList || isCurrentUserCreator;
    }

    public static String[] getPath(String path) {
        if (path == null || path.equals(STRING_EMPTY)) {
            return new String[0];
        }
        if (path.lastIndexOf(';') == path.length() - 1) {
            path = path.substring(0, path.lastIndexOf(';'));
        }
        return path.split(";");
    }

    public static Boolean isCurrentUserAdministrator() throws JenkinsInstanceNotFoundException {
        Jenkins inst = BuildConfigurationManager.getJenkins();
        Permission permission = Jenkins.ADMINISTER;

        if (inst != null) {
            return inst.hasPermission(permission);
        } else {
            List<Ancestor> ancs = Stapler.getCurrentRequest().getAncestors();
            for (Ancestor anc : Iterators.reverse(ancs)) {
                Object o = anc.getObject();
                if (o instanceof AccessControlled) {
                    return ((AccessControlled) o).hasPermission(permission);
                }
            }
            return BuildConfigurationManager.getJenkins().hasPermission(permission);
        }
    }

    public static BuildConfigurationModel getConfiguration(String name) throws IOException, JAXBException {

        if (!isCurrentUserHasAccess(name)) {
            return null;
        }
        return load(name);
    }

    public static String getAdminEmail() {
        return JenkinsLocationConfiguration.get().getAdminAddress();
    }

    public static String getUserMailAddress(BuildConfigurationModel config) {
        if (config.getConfigEmail() != null) {
            String[] address = config.getConfigEmail().split(" ");
            return StringUtils.join(address, ",");
        }
        return StringUtils.EMPTY;
    }

    public static List<String> getSCM() throws JenkinsInstanceNotFoundException {
        List<String> supportedSCMs = new ArrayList<>();
        boolean isGitCatch = false;
        boolean isSubversionCatch = false;
        for (SCMDescriptor<?> scm : hudson.scm.SCM.all()) {
            if (isSupportedSCM(scm)) {
                supportedSCMs.add(scm.getDisplayName());
                if (scm.getDisplayName().equalsIgnoreCase("git")) {
                    isGitCatch = true;
                } else if (scm.getDisplayName().equalsIgnoreCase("subversion")) {
                    isSubversionCatch = true;
                }
            }
        }
        if (isGitCatch) {
            log.info("+++++ git: plugin was plugged");
        } else {
            log.info("----- git: plugin wasn't plugged");
        }
        if (isSubversionCatch) {
            log.info("+++++ subversion: plugin was plugged");
        } else {
            log.info("----- subversion: plugin wasn't plugged");
        }

        return supportedSCMs;
    }

    private static Boolean isSupportedSCM(SCMDescriptor<?> scm) throws JenkinsInstanceNotFoundException {
        scmLoader = new SCMLoader();
        for (SCM supportSCM : scmLoader.getSCMs()) {
            if (supportSCM.getKey().equalsIgnoreCase(scm.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getNodesName() throws JenkinsInstanceNotFoundException {
        List<String> nodeNames = new ArrayList<>();
        for (Node node : BuildConfigurationManager.getJenkins().getNodes()) {
            nodeNames.add(node.getNodeName());
        }
        return nodeNames;
    }

    public static void createJob(String name)
            throws IOException, ParserConfigurationException,
            SAXException, TransformerException, JAXBException, XPathExpressionException {
        BuildConfigurationModel config = load(name);
        JobManagerGenerator.createJob(config);
        config.setJobUpdate(true);
        save(config);
    }

    public static void deleteJob(String name)
            throws IOException, InterruptedException, ParserConfigurationException, JAXBException {
        JobManagerGenerator.deleteJob(name);
        BuildConfigurationModel config = BuildConfigurationManager.load(name);
        if (config.getProjectName() != null && config.getState().equals(ConfigurationState.APPROVED)) {

            config.setJobUpdate(false);
            BuildConfigurationManager.save(config);
        }
    }

    public static void setDefaultCredentials(String credentials) throws JenkinsInstanceNotFoundException {
        Properties prop = new Properties();
        File path = getRootDir();
        if (!path.exists()) {
            path.mkdirs();
        }
        try (OutputStream output = new FileOutputStream(path + "/" + defaultCredentialsPropertiesFileName);){
            prop.setProperty(credentialsPropertyName, credentials);
            prop.store(output, null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getDefaultCredentials() throws JenkinsInstanceNotFoundException {
        Properties prop = new Properties();
        File propertiesFile = new File(getRootDir(), defaultCredentialsPropertiesFileName);
        String defaultCredentials = null;
        if (!propertiesFile.exists()) {
            return null;
        }

        try (InputStream input = new FileInputStream(propertiesFile)) {
            // load a properties file
            prop.load(input);
            defaultCredentials = prop.getProperty(credentialsPropertyName);


        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return defaultCredentials;
    }

    public static List<CredentialItem> openCredentials() throws IOException {

        String jenkinsHomePath = BuildConfigurationManager.getJenkins().getRootDir().getPath();
        List<CredentialItem> credentialItemList = new ArrayList<>();
        String fileName = jenkinsHomePath + "/credentials.xml";

        try {
            File fXmlFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("java.util.concurrent.CopyOnWriteArrayList");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                org.w3c.dom.Node nNode = nList.item(temp);
                if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    NodeList nodeList = eElement.getChildNodes();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        org.w3c.dom.Node curNode = nodeList.item(i);
                        if (curNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                            Element curElement = (Element) curNode;
                            CredentialItem crItem = new CredentialItem();
                            if (curElement.getElementsByTagName("scope").item(0) != null) {
                                if (curElement.getElementsByTagName("scope").item(0).getTextContent() != null) {
                                    crItem.setScope(curElement.getElementsByTagName("scope").item(0).getTextContent());
                                }
                            }
                            if (curElement.getElementsByTagName("id").item(0) != null) {
                                if (curElement.getElementsByTagName("id").item(0).getTextContent() != null) {
                                    crItem.setId(curElement.getElementsByTagName("id").item(0).getTextContent());
                                }
                            }
                            if (curElement.getElementsByTagName("username").item(0) != null) {
                                if (curElement.getElementsByTagName("username").item(0).getTextContent() != null) {
                                    crItem.setUsername(curElement.getElementsByTagName("username").item(0).getTextContent());
                                }
                            }
                            if (curElement.getElementsByTagName("description").item(0) != null) {
                                if (curElement.getElementsByTagName("description").item(0).getTextContent() != null) {
                                    crItem.setDescription(curElement.getElementsByTagName("description").item(0).getTextContent());
                                }
                            }
                            credentialItemList.add(crItem);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return credentialItemList;
    }

    public static Jenkins getJenkins() throws JenkinsInstanceNotFoundException {
        Jenkins instance = Jenkins.getInstance();
        if (instance == null) {
            throw new JenkinsInstanceNotFoundException("Jenkins instance not found");
        }
        return Jenkins.getInstance();
    }
}
