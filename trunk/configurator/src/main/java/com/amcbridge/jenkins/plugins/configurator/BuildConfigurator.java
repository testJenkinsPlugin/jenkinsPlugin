package com.amcbridge.jenkins.plugins.configurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.jelly.JellyException;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.tmatesoft.svn.core.SVNException;
import org.xml.sax.SAXException;
import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.enums.*;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.messenger.*;
import com.amcbridge.jenkins.plugins.view.ProjectToBuildView;
import com.amcbridge.jenkins.plugins.view.ViewGenerator;
import com.amcbridge.jenkins.plugins.vsc.VersionControlSystemResult;
import com.amcbridge.jenkins.plugins.xmlSerialization.Job;
import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.User;

@Extension
public final class BuildConfigurator implements RootAction {

    private MailSender mail;
    private static Boolean isCommited;

    private static final String VIEW_GENERATOR = "viewGenerator";
    private static final String PLUGIN_NAME = "Build Configurator";
    private static final String ICON_PATH = "/plugin/configurator/icons/system_config_services.png";
    private static final String DEFAULT_PAGE_URL = "BuildConfigurator";
    private String editedProjectName = "";

    public BuildConfigurator() {
        mail = new MailSender();
        try {
            isCommited = BuildConfigurationManager.isCommited();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDisplayName() {
        if (User.current() == null) {
            return null;
        }
        return PLUGIN_NAME;
    }

    public String getIconFileName() {
        if (User.current() == null) {
            return null;
        }
        return ICON_PATH;
    }

    public String getUrlName() {
        if (User.current() == null) {
            return null;
        }
        return DEFAULT_PAGE_URL;
    }

    public List<BuildConfigurationModel> getAllConfigurations()
            throws IOException, ServletException, JAXBException {
        return BuildConfigurationManager.loadAllConfigurations();
    }

    public void doCreateNewConfigurator(final StaplerRequest request,
            final StaplerResponse response) throws
            IOException, ServletException, ParserConfigurationException, JAXBException,
            AddressException, MessagingException {

        JSONObject formAttribute = request.getSubmittedForm();

        BuildConfigurationModel newConfig = new BuildConfigurationModel();

        request.bindJSON(newConfig, formAttribute);

        newConfig.setScripts(BuildConfigurationManager
                .getPath(formAttribute.get("scripts").toString()));
        if (formAttribute.get("build_machine_configuration") != null) {
            newConfig.setBuildMachineConfiguration(BuildConfigurationManager
                    .getPath(formAttribute.get("build_machine_configuration").toString()));
        }
        newConfig.setCurrentDate();
        newConfig.setJobUpdate(true);

        ConfigurationStatusMessage message
                = new ConfigurationStatusMessage(newConfig.getProjectName());
        message.setSubject(newConfig.getProjectName());

        FormResult type = FormResult.valueOf(formAttribute.get("formType").toString());

        BuildConfigurationModel currentConfig = BuildConfigurationManager
                .load(newConfig.getProjectName());

        switch (type) {
            case CREATE:
                newConfig.setState(ConfigurationState.NEW);
                message.setDescription(MessageDescription.CREATE.toString());
                break;
            case EDIT:
                newConfig.setState(ConfigurationState.UPDATED);
                message.setDescription(MessageDescription.CHANGE.toString());
                break;
            case APPROVED:
                newConfig.setState(ConfigurationState.APPROVED);
                newConfig.setCreator(currentConfig.getCreator());
                newConfig.setJobUpdate(false);
                BuildConfigurationManager.save(newConfig);

                Job newJob = new Job(newConfig);
                Job currentJob = BuildConfigurationManager.acm.get(newConfig.getProjectName());
                if (!newJob.equals(currentJob)) {
                    BuildConfigurationManager.acm.add(newJob);
                    isCommited = BuildConfigurationManager.isCommited();
                }
                message.setDescription(MessageDescription.APPROVE.toString());
                if (!BuildConfigurationManager.getUserMailAddress(newConfig).isEmpty()) {
                    message.setCC(BuildConfigurationManager.getUserMailAddress(newConfig));
                }
                break;
            case REJECT:
                newConfig = currentConfig;
                newConfig.setState(ConfigurationState.REJECTED);
                message.setDescription(MessageDescription.REJECT.toString()
                        + " " + formAttribute.get("rejectionReason").toString());
                if (!BuildConfigurationManager.getUserMailAddress(newConfig).isEmpty()) {
                    message.setCC(BuildConfigurationManager.getUserMailAddress(newConfig));
                }
                newConfig.setRejectionReason(formAttribute.get("rejectionReason").toString());
                break;
            default:
                break;
        }
        editedProjectName = newConfig.getProjectName();
        BuildConfigurationManager.save(newConfig);
        message.setDestinationAddress(getAdminEmails());
        mail.sendMail(message);
        response.sendRedirect("./");
    }

    public String doUploadFile(final HttpServletRequest request,
            final HttpServletResponse response) throws FileUploadException, IOException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List items = upload.parseRequest(request);
        Iterator iter = items.iterator();
        FileItem item = (FileItem) iter.next();
        byte[] data = item.get();
        String path = BuildConfigurationManager.getUserContentFolder();
        File checkFile = new File(path);
        if (!checkFile.exists()) {
            checkFile.mkdirs();
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();
        String fileName = item.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "("
                + dateFormat.format(date) + ")"
                + fileName.substring(fileName.lastIndexOf('.'));
        File saveFile = new File(path, fileName);
        saveFile.createNewFile();
        OutputStream os = new FileOutputStream(saveFile);
        try {
            os.write(data);
        } finally {
            os.close();
        }
        return fileName;
    }

    @JavaScriptMethod
    public ProjectToBuildView getView()
            throws UnsupportedEncodingException, JellyException {
        if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
            loadCreateNewBuildConfiguration();
        }

        return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                .getProjectToBuildlView();
    }

    @JavaScriptMethod
    public ProjectToBuildView loadViews(String projectName)
            throws JellyException, IOException, JAXBException {
        BuildConfigurationModel conf = BuildConfigurationManager.load(projectName);

        if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
            loadCreateNewBuildConfiguration();
        }

        return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                .getProjectToBuildlView(conf.getProjectToBuild());
    }

    @JavaScriptMethod
    public ProjectToBuildView getBuilderView()
            throws UnsupportedEncodingException, JellyException {
        if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
            loadCreateNewBuildConfiguration();
        }

        return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                .getBuilderView();
    }

    @JavaScriptMethod
    public static void deleteNotUploadFile(String[] files) {
        String pathFolder = BuildConfigurationManager.getUserContentFolder();
        BuildConfigurationManager.deleteFiles(files, pathFolder);
    }

    @JavaScriptMethod
    public void setForDeletion(String name) throws AddressException,
            IOException, ParserConfigurationException, JAXBException, MessagingException {
        BuildConfigurationManager.markConfigurationForDeletion(name);
    }

    @JavaScriptMethod
    public VersionControlSystemResult exportToXml()
            throws SVNException, IOException, InterruptedException {
        VersionControlSystemResult result = BuildConfigurationManager.exportToXml(editedProjectName);
        if (result.getSuccess()) {
            isCommited = true;
        } else {
            File file = BuildConfigurationManager.getFileToExportConfigurations();
            if (file.exists()) {
                file.delete();
            }
        }
        return result;
    }

    @JavaScriptMethod
    public void loadCreateNewBuildConfiguration() {
        Stapler.getCurrentRequest().getSession().setAttribute(VIEW_GENERATOR, new ViewGenerator());
    }

    @JavaScriptMethod
    public Boolean isNameFree(String name) {
        return !BuildConfigurationManager.isNameUsing(name);
    }

    @JavaScriptMethod
    public String getFullNameCreator(String creator) {
        User user = User.current().get(creator);
        String fullname = user.getFullName();
        return fullname;
    }

    @JavaScriptMethod
    public void deleteConfigurationPermanently(String name)
            throws AddressException, IOException, MessagingException, JAXBException, InterruptedException, ParserConfigurationException {
        BuildConfigurationManager.deleteConfigurationPermanently(name);
        isCommited = BuildConfigurationManager.isCommited();
    }

    @JavaScriptMethod
    public void restoreConfiguration(String name)
            throws AddressException, IOException, MessagingException, JAXBException, ParserConfigurationException {
        BuildConfigurationManager.restoreConfiguration(name);
    }

    public static Boolean isCurrentUserCreator(String name) throws IOException, JAXBException {
        return BuildConfigurationManager.isCurrentUserCreatorOfConfiguration(name);
    }

    @JavaScriptMethod
    public static Boolean isCurrentUserAdministrator() {
        return BuildConfigurationManager.isCurrentUserAdministrator();
    }

    @JavaScriptMethod
    public BuildConfigurationModel getConfiguration(String name) throws IOException, JAXBException {
        return BuildConfigurationManager.getConfiguration(name);
    }

    public static String getAdminEmails() {
        return BuildConfigurationManager.getAdminEmail();
    }

    public List<String> getSCM() {
        return BuildConfigurationManager.getSCM();
    }

    public List<String> getNodesName() {
        return BuildConfigurationManager.getNodesName();
    }

    @JavaScriptMethod
    public void createJob(String name)
            throws IOException, ParserConfigurationException,
            SAXException, TransformerException, JAXBException {
        BuildConfigurationManager.createJob(name);
    }

    @JavaScriptMethod
    public Boolean isJobCreated(String name) {
        return JobManagerGenerator.isJobExist(JobManagerGenerator.validJobName(name));
    }

    @JavaScriptMethod
    public void deleteJob(String name)
            throws IOException, InterruptedException, ParserConfigurationException, JAXBException {
        BuildConfigurationManager.deleteJob(name);
    }

    @JavaScriptMethod
    public Boolean isCommited() {
        return isCommited;
    }
}
