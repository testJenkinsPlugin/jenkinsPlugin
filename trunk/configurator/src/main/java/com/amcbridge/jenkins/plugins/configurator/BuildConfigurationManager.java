package com.amcbridge.jenkins.plugins.configurator;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.serialization.CredentialItem;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import com.amcbridge.jenkins.plugins.enums.MessageDescription;
import com.amcbridge.jenkins.plugins.enums.SCMElement;
import com.amcbridge.jenkins.plugins.enums.SCMLoader;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.messenger.*;
import com.amcbridge.jenkins.plugins.vsc.CommitError;
import com.amcbridge.jenkins.plugins.vsc.GitManager;
import com.amcbridge.jenkins.plugins.vsc.SvnManager;
import com.amcbridge.jenkins.plugins.vsc.VersionControlSystem;
import com.amcbridge.jenkins.plugins.vsc.VersionControlSystemResult;
import com.amcbridge.jenkins.plugins.xmlSerialization.*;
import com.amcbridge.jenkins.plugins.xmlSerialization.ExportSettings.Settings;
import hudson.XmlFile;
import hudson.model.Node;
import hudson.model.User;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.util.Iterators;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BuildConfigurationManager {

    public static final String CONFIG_FILE_NAME = "config.xml";
    public static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final String ENCODING = "UTF-8";
    private static final String CONFIG_JOB_FILE_NAME = "JobConfig.xml";
    private static final String BUILD_CONFIGURATOR_DIRECTORY_NAME = "\\plugins\\BuildConfiguration";
    private static final String CONTENT_FOLDER = "userContent";
    private static final String SCRIPT_FOLDER = "Scripts";
    private static final String CHECK_CONFIG_FILE_NAME = "configCheck.xml";
    private static final Integer MAX_FILE_SIZE = 1048576;//max file size which equal 1 mb in bytes
    private static final String[] SCRIPTS_EXTENSIONS = {"bat", "nant", "powershell", "shell",
        "ant", "maven"};
    public static final String STRING_EMPTY = "";
    public static ApproveConfigurationManager acm = new ApproveConfigurationManager();
    private static final MailSender mail = new MailSender();
    private static final ReentrantLock lock = new ReentrantLock();
    private static String currentScm = "None";
    private static String currentScm4Config = "None";
    private static final Logger log = LoggerFactory.getLogger(BuildConfigurationManager.class);
    private static final SCMLoader scmLoader = new SCMLoader();

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

    public static File getFileToExportConfigurations() {
        return new File(getRootDir() + "\\" + CONFIG_FILE_NAME);
    }

    public static File getFileForCheckVSC() {
        return new File(getRootDir() + "\\" + CHECK_CONFIG_FILE_NAME);
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
        saveFile(config);
    }

    public static void saveFile(BuildConfigurationModel config) {
        if (config.getProjectName().isEmpty()) {
            return;
        }

        String pathFolder = getRootDirectory() + "\\" + config.getProjectName()
                + "\\" + SCRIPT_FOLDER;
        String filePath;
        File checkFolder = new File(pathFolder);
        File checkFile;

        if (config.getScripts().length == 0) {
            if (checkFolder.exists()) {
                FileUtils.deleteQuietly(checkFolder);
            }
            return;
        }

        if (!checkFolder.exists()) {
            checkFolder.mkdirs();
        }

        String[] scripts = config.getScripts();

        for (int i = 0; i < scripts.length; i++) {
            if (scripts[i].isEmpty()) {
                continue;
            }
            filePath = getUserContentFolder() + "\\" + scripts[i];
            checkFile = new File(filePath);
            if (!checkFile.exists()) {
                checkFile = new File(pathFolder + "\\" + scripts[i]);
                if (!checkFile.exists()) {
                    scripts[i] = STRING_EMPTY;
                }
                continue;
            }
            if (checkFile.length() < MAX_FILE_SIZE && checkExtension(checkFile.getName())) {
                checkFile.renameTo(new File(pathFolder + "\\"
                        + checkFile.getName()));
            } else {
                scripts[i] = STRING_EMPTY;
            }
        }

        config.setScripts(scripts);
        checkFile = new File(pathFolder);
        File[] listOfFiles = checkFile.listFiles();

        for (File listOfFile : listOfFiles) {
            if (ArrayUtils.indexOf(scripts, listOfFile.getName()) == -1) {
                listOfFile.delete();
            }
        }
    }

    private static Boolean checkExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        for (String script : SCRIPTS_EXTENSIONS) {
            if (script.equals(extension)) {
                return true;
            }
        }
        return false;
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
            JAXBException, AddressException, MessagingException {
        BuildConfigurationModel config = load(name);
        if (config.getState() == ConfigurationState.FOR_DELETION) {
            return;
        }
        config.setState(ConfigurationState.FOR_DELETION);
        config.setCurrentDate();
        save(config);
        ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName(),
                getAdminEmail(), StringUtils.EMPTY, MessageDescription.MARKED_FOR_DELETION.toString(),
                config.getProjectName());
        mail.sendMail(message);
    }

    public static void restoreConfiguration(String name) throws IOException, ParserConfigurationException,
            JAXBException, AddressException, MessagingException {
        BuildConfigurationModel config = load(name);
        config.setState(ConfigurationState.UPDATED);
        currentScm = config.getScm();
        save(config);
        ConfigurationStatusMessage message = new ConfigurationStatusMessage(config.getProjectName(),
                getAdminEmail(), getUserMailAddress(config), MessageDescription.RESTORE.toString(),
                config.getProjectName());
        mail.sendMail(message);

    }

    public static VersionControlSystemResult exportToXml(/*String editedProjectName*/)
            throws SVNException, IOException, InterruptedException {
        lock.lock();
        try {
            XmlExporter xmlExporter = new XmlExporter();
            String path = xmlExporter.exportToXml(true);

//            BuildConfigurationModel config = load(editedProjectName);
//            currentScm = config.getScm();

            VersionControlSystem vcs = new SvnManager();

            Settings settings = new Settings();

            currentScm4Config = settings.getTypeSCM4Config();

            if (currentScm4Config.equalsIgnoreCase("Subversion")) {
                vcs = new SvnManager();
            } else if (currentScm4Config.equalsIgnoreCase("Git")) {
                vcs = new GitManager();
            }

            if (!settings.isSettingsSet()) {
                VersionControlSystemResult result = new VersionControlSystemResult(false);
                result.setErrorMassage(CommitError.NONE_PROPERTY.toString());
                return result;
            }

            if (currentScm4Config.equalsIgnoreCase("Git")) {
                ((GitManager) vcs).setLocalRepoPath(settings.getLocalGitRepoPath());
//                ((GitManager) vcs).setProjectName(editedProjectName);
                ((GitManager) vcs).setBranch(settings.getBranch());
            }

            return vcs.doCommit(path, settings.getUrl(), settings.getLogin(),
                    settings.getPassword(), settings.getCommitMessage());
        } finally {
            lock.unlock();
        }
    }

    public static Boolean isNameUsing(String name) {
        File checkName = new File(getRootDirectory() + "\\" + name);
        return checkName.exists();
    }

    public static void deleteConfigurationPermanently(String name) throws IOException,
            AddressException, MessagingException, JAXBException, InterruptedException, ParserConfigurationException {
        File checkFile = new File(getRootDirectory() + "\\" + name);
        BuildConfigurationModel config = load(name);
        if (checkFile.exists()) {
            FileUtils.deleteDirectory(checkFile);
        }

        deleteJob(name);
        acm.remove(name);

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
        Object inst = Jenkins.getInstance();
        Permission permission = Jenkins.ADMINISTER;

        if (inst instanceof AccessControlled) {
            return ((AccessControlled) inst).hasPermission(permission);
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
        BuildConfigurationModel currenConfig = load(name);
        if (!isCurrentUserAdministrator() && !isCurrentUserCreatorOfConfiguration(name)) {
            return null;
        }
        return currenConfig;
    }

    public static String getAdminEmail() {
        String adminMail = JenkinsLocationConfiguration.get().getAdminAddress();
        return adminMail;
    }

    public static List<String> getSCM() {
        List<String> result = new ArrayList<String>();
        boolean isGitCatch = false;
        boolean isSubversionCatch = false;
        for (SCMDescriptor<?> scm : SCM.all()) {
            if (isSupportedSCM(scm)) {
                result.add(scm.getDisplayName());
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

        return result;
    }

    private static Boolean isSupportedSCM(SCMDescriptor<?> scm) {
        for (SCMElement suportSCM : scmLoader.getSCMs()) {
            if (suportSCM.getKey().equalsIgnoreCase(scm.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getNodesName() {
        List<String> result = new ArrayList<String>();
        for (Node node : Jenkins.getInstance().getNodes()) {
            result.add(node.getNodeName());
        }

        return result;
    }

    public static String getUserMailAddress(BuildConfigurationModel config) {
        if (config.getConfigEmail() != null) {
            String[] address = config.getConfigEmail().split(" ");
            return StringUtils.join(address, ",");
        }
        return StringUtils.EMPTY;
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

    public static Boolean isCommited() throws IOException {
        if (!getFileToExportConfigurations().exists()) {
            return !(XmlExporter.generateConfiguration().size() > 0);
        }

        File svnVersion = getFileToExportConfigurations();
        XmlExporter xmlExporter = new XmlExporter();
        String path = xmlExporter.exportToXml(false);
        File currentVersion = new File(path);
        return FileUtils.contentEquals(svnVersion, currentVersion);
    }

    public static List<CredentialItem> openCredentials() throws IOException {
        String jenkinsHomePath = Jenkins.getInstance().getRootDir().getPath();
        if (jenkinsHomePath == null) {
            return new ArrayList();
        }
        List<CredentialItem> credentialItemList = new ArrayList<CredentialItem>();

        String fileName = changeFilePath(jenkinsHomePath) + "/credentials.xml";
        try {

            File fXmlFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("java.util.concurrent.CopyOnWriteArrayList");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                org.w3c.dom.Node nNode = nList.item(temp);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    NodeList nodeList = eElement.getChildNodes();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        org.w3c.dom.Node curNode = nodeList.item(i);
                        if (curNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                            System.out.println("\nCurrent Element :" + curNode.getNodeName());
                            Element curElement = (Element) curNode;
                            CredentialItem crItem = new CredentialItem();
                            if (curElement.getElementsByTagName("scope").item(0) != null) {
                                if (curElement.getElementsByTagName("scope").item(0).getTextContent() != null) {
                                    crItem.setScope(curElement.getElementsByTagName("scope").item(0).getTextContent());
                                    System.out.println("scope : " + curElement.getElementsByTagName("scope").item(0).getTextContent());
                                }
                            }
                            if (curElement.getElementsByTagName("id").item(0) != null) {
                                if (curElement.getElementsByTagName("id").item(0).getTextContent() != null) {
                                    crItem.setId(curElement.getElementsByTagName("id").item(0).getTextContent());
                                    System.out.println("id : " + curElement.getElementsByTagName("id").item(0).getTextContent());
                                }
                            }
                            if (curElement.getElementsByTagName("username").item(0) != null) {
                                if (curElement.getElementsByTagName("username").item(0).getTextContent() != null) {
                                    crItem.setUsername(curElement.getElementsByTagName("username").item(0).getTextContent());
                                    System.out.println("username : " + curElement.getElementsByTagName("username").item(0).getTextContent());
                                }
                            }
                            if (curElement.getElementsByTagName("description").item(0) != null) {
                                if (curElement.getElementsByTagName("description").item(0).getTextContent() != null) {
                                    crItem.setDescription(curElement.getElementsByTagName("description").item(0).getTextContent());
                                    System.out.println("description : " + curElement.getElementsByTagName("description").item(0).getTextContent());
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

    public static String changeFilePath(String filePath) {
        String realFilePath = "";
        if (filePath != null) {
            filePath = filePath.replace("\\", "/");
            for (int i = 0; i < filePath.length(); i++) {
                if (filePath.charAt(i) != '.') {
                    realFilePath += filePath.charAt(i);
                } else {
                    if (i < filePath.length() - 5) {
                        i++;
                    } else {
                        realFilePath += filePath.charAt(i);
                    }
                }
            }
        }
        return realFilePath;
    }

}
