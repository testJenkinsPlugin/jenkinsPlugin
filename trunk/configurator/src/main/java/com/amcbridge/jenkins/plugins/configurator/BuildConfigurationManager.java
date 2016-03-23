package com.amcbridge.jenkins.plugins.configurator;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import com.amcbridge.jenkins.plugins.enums.MessageDescription;
import com.amcbridge.jenkins.plugins.xstreamElements.SCM;
import com.amcbridge.jenkins.plugins.xstreamElements.SCMLoader;
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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BuildConfigurationManager {

    public static final String CONFIG_FILE_NAME = "config.xml";
    public static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final String ENCODING = "UTF-8";
    private static final String CONFIG_JOB_FILE_NAME = "JobConfig.xml";
    private static final String BUILD_CONFIGURATOR_DIRECTORY_NAME = "\\plugins\\BuildConfiguration";
    private static final String CONTENT_FOLDER = "userContent";
    public static final String STRING_EMPTY = "";
    private static final MailSender mail = new MailSender();
    private static final Logger log = LoggerFactory.getLogger(BuildConfigurationManager.class);
    private static final SCMLoader scmLoader = new SCMLoader();
    private static String defaultCredentialsPropertiesFileName = "credentialsDefaults.properties";
    private static String credentialsPropertyName = "defaultCredentials";

    public static String getCurrentUserID() {
        if (User.current() != null) {
            return User.current().getId();
        } else {
            return STRING_EMPTY;
        }
    }

    public static File getConfigFileFor(String id) {
        return new File(new File(getRootDir(), id), CONFIG_FILE_NAME);
    }


    public static File getFileToCreateJob() {
        return new File(getRootDir() + "\\" + CONFIG_JOB_FILE_NAME);
    }

    static File getRootDir() {
        return new File(Jenkins.getInstance().getRootDir(),
                BUILD_CONFIGURATOR_DIRECTORY_NAME);
    }

    public static String getRootDirectory() {
        return Jenkins.getInstance().getRootDir() + "\\"
                + BUILD_CONFIGURATOR_DIRECTORY_NAME;
    }

    public static String getUserContentFolder() {
        return Jenkins.getInstance().getRootDir() + "\\" + CONTENT_FOLDER;
    }

    public static void save(BuildConfigurationModel config) throws IOException,
            ParserConfigurationException, JAXBException {
        if (config.getProjectName().isEmpty()) {
            deleteFiles(config.getScripts(), getUserContentFolder());
            return;
        }

        File checkFile = new File(getRootDirectory() + "\\" + config.getProjectName());
        if (!checkFile.exists()) {
            checkFile.mkdirs();
        }

        XmlFile fileWriter = getConfigFile(config.getProjectName());
        fileWriter.write(config);
    }


    protected final static XmlFile getConfigFile(String nameProject) {
        return new XmlFile(Jenkins.XSTREAM, getConfigFileFor("\\" + nameProject));
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
        List<BuildConfigurationModel> configs = new ArrayList<BuildConfigurationModel>();
        File file = new File(getRootDirectory());

        if (!file.exists()) {
            return null;
        }

        File[] directories = file.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        for (File directory : directories) {
            if (!isCurrentUserAdministrator() && !isCurrentUserCreatorOfConfiguration(directory.getName())) {
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
            file = new File(pathFolder + "\\" + strFile);
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
        ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName(),
                getAdminEmail(), StringUtils.EMPTY, MessageDescription.MARKED_FOR_DELETION.toString(),
                config.getProjectName());
        mail.sendMail(message);
    }

    public static void restoreConfiguration(String name) throws IOException, ParserConfigurationException,
            JAXBException, MessagingException {
        BuildConfigurationModel config = load(name);
        config.setState(ConfigurationState.UPDATED);
        save(config);
        ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName(),
                getAdminEmail(), getUserMailAddress(config), MessageDescription.RESTORE.toString(),
                config.getProjectName());
        mail.sendMail(message);
    }


    public static Boolean isNameUsing(String name) {
        File checkName = new File(getRootDirectory() + "\\" + name);
        return checkName.exists();
    }

    public static void deleteConfigurationPermanently(String name) throws IOException,
            MessagingException, JAXBException, InterruptedException, ParserConfigurationException {
        File checkFile = new File(getRootDirectory() + "\\" + name);
        BuildConfigurationModel config = load(name);
        if (checkFile.exists()) {
            FileUtils.deleteDirectory(checkFile);
        }

        deleteJob(name);

        ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName());
        message.setSubject(config.getProjectName());
        message.setDescription(MessageDescription.DELETE_PERMANENTLY.toString());

        message.setCC(getUserMailAddress(config));
        message.setDestinationAddress(getAdminEmail());
        mail.sendMail(message);
    }

    public static Boolean isCurrentUserCreatorOfConfiguration(String name) throws IOException, JAXBException {
        return load(name).getCreator().equals(getCurrentUserID());
    }

    public static String[] getPath(String value) {
        if (value.equals(STRING_EMPTY)) {
            return new String[0];
        }
        if (value.lastIndexOf(';') == value.length() - 1) {
            value = value.substring(0, value.lastIndexOf(';'));
        }
        return value.split(";");
    }

    public static Boolean isCurrentUserAdministrator() {
        Jenkins inst = Jenkins.getInstance();
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
            return Jenkins.getInstance().hasPermission(permission);
        }
    }

    public static BuildConfigurationModel getConfiguration(String name) throws IOException, JAXBException {
        BuildConfigurationModel currentConfig = load(name);
        if (!isCurrentUserAdministrator() && !isCurrentUserCreatorOfConfiguration(name)) {
            return null;
        }
        return currentConfig;
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

    public static List<String> getSCM() {
        List<String> supportedSCMs = new ArrayList<String>();
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

    private static Boolean isSupportedSCM(SCMDescriptor<?> scm) {
        for (SCM supportSCM : scmLoader.getSCMs()) {
            if (supportSCM.getKey().equalsIgnoreCase(scm.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getNodesName() {
        List<String> nodeNames = new ArrayList<String>();
        for (Node node : Jenkins.getInstance().getNodes()) {
            nodeNames.add(node.getNodeName());
        }
        return nodeNames;
    }

    public static void createJob(String name)
            throws IOException, ParserConfigurationException,
            SAXException, TransformerException, JAXBException {
        BuildConfigurationModel config = load(name);
        JobManagerGenerator.createJob(config);
        config.setJobUpdate(true);
        save(config);
    }

    public static void deleteJob(String name)
            throws IOException, InterruptedException, ParserConfigurationException, JAXBException {
        JobManagerGenerator.deleteJob(name);
        BuildConfigurationModel config = BuildConfigurationManager.load(name);
        if (config.getProjectName() != null) {
            if (config.getState().equals(ConfigurationState.APPROVED)) {
                config.setJobUpdate(false);
                BuildConfigurationManager.save(config);
            }
        }
    }

    public static void setDefaultCredentials(String credentials) {
        Properties prop = new Properties();
        OutputStream output = null;
        try {
            File path = getRootDir();
            if (!path.exists()) {
                path.mkdirs();
            }
            output = new FileOutputStream(path + "\\" + defaultCredentialsPropertiesFileName);
            prop.setProperty(credentialsPropertyName, credentials);
            prop.store(output, null);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static String getDefaultCredentials() {
        Properties prop = new Properties();
        InputStream input = null;
        File propertiesFile = new File(getRootDir(), defaultCredentialsPropertiesFileName);
        String defaultCredentials = null;
        if (!propertiesFile.exists()) {
            return null;
        }

        try {

            input = new FileInputStream(propertiesFile);

            // load a properties file
            prop.load(input);
            defaultCredentials = prop.getProperty(credentialsPropertyName);


        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return defaultCredentials;
    }

    public static List<CredentialItem> openCredentials() throws IOException {

        String jenkinsHomePath = Jenkins.getInstance().getRootDir().getPath();
        List<CredentialItem> credentialItemList = new ArrayList<CredentialItem>();
        String fileName = jenkinsHomePath + "\\credentials.xml";

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
}
