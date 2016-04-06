package com.amcbridge.jenkins.plugins.configurator;

import com.amcbridge.jenkins.plugins.configurationModels.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import com.amcbridge.jenkins.plugins.enums.FormResult;
import com.amcbridge.jenkins.plugins.enums.MessageDescription;
import com.amcbridge.jenkins.plugins.job.JobManagerGenerator;
import com.amcbridge.jenkins.plugins.messenger.ConfigurationStatusMessage;
import com.amcbridge.jenkins.plugins.messenger.MailSender;
import com.amcbridge.jenkins.plugins.view.ProjectToBuildView;
import com.amcbridge.jenkins.plugins.view.ViewGenerator;
import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.User;
import net.sf.json.JSONObject;
import org.apache.commons.jelly.JellyException;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.xml.sax.SAXException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

@Extension
public final class BuildConfigurator implements RootAction {

    private MailSender mail;
    private static final String VIEW_GENERATOR = "viewGenerator";
    private static final String PLUGIN_NAME = "Build Configurator";
    private static final String ICON_PATH = "/plugin/build-configurator/icons/system_config_services.png";
    private static final String DEFAULT_PAGE_URL = "BuildConfigurator";

    public BuildConfigurator() {
        mail = new MailSender();
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
            MessagingException {

        JSONObject formAttribute = request.getSubmittedForm();

        String newDefaultCredentials = formAttribute.get("default_credentials") != null ? formAttribute.get("default_credentials").toString() : null;
        BuildConfigurationModel newConfig = new BuildConfigurationModel();

        request.bindJSON(newConfig, formAttribute);

        if (formAttribute.get("build_machine_configuration") != null) {
            newConfig.setBuildMachineConfiguration(BuildConfigurationManager
                    .getPath(formAttribute.get("build_machine_configuration").toString()));
        }
        newConfig.initCurrentDate();
        newConfig.setJobUpdate(true);
        if (newDefaultCredentials != null && !newDefaultCredentials.isEmpty() && isCurrentUserAdministrator()) {
            BuildConfigurationManager.setDefaultCredentials(newDefaultCredentials);
        }
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
                newConfig.setCreator(currentConfig.getCreator());
                newConfig.setState(ConfigurationState.UPDATED);
                message.setDescription(MessageDescription.CHANGE.toString());
                break;
            case APPROVED:
                newConfig.setState(ConfigurationState.APPROVED);
                newConfig.setCreator(currentConfig.getCreator());
                newConfig.setJobUpdate(false);
                BuildConfigurationManager.save(newConfig);
                message.setDescription(MessageDescription.APPROVE.toString());

                break;
            case REJECT:
                newConfig = currentConfig;
                newConfig.setState(ConfigurationState.REJECTED);
                message.setDescription(MessageDescription.REJECT.toString()
                        + " " + formAttribute.get("rejectionReason").toString());
                newConfig.setRejectionReason(formAttribute.get("rejectionReason").toString());
                break;
            default:
                break;
        }
        if (!BuildConfigurationManager.getUserMailAddress(newConfig).isEmpty()) {
            message.setCC(BuildConfigurationManager.getUserMailAddress(newConfig));
        }

        BuildConfigurationManager.save(newConfig);
        message.setDestinationAddress(getAdminEmails());
        mail.sendMail(message);
        response.sendRedirect("./");
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

 /*   @JavaScriptMethod
    public static void deleteNotUploadFile(String[] files) {
        String pathFolder = BuildConfigurationManager.getUserContentFolder();
        BuildConfigurationManager.deleteFiles(files, pathFolder);
    }*/

    @JavaScriptMethod
    public void setForDeletion(String name) throws AddressException,
            IOException, ParserConfigurationException, JAXBException, MessagingException {
        BuildConfigurationManager.markConfigurationForDeletion(name);
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
    public boolean isJenkinsEmailConfigOK() {
        Properties prop = new Properties();
        InputStream inputStream = null;
        boolean isEmailPropertiesOK = false;
        boolean isPortOk = false;
        try {
            inputStream = new FileInputStream(MailSender.getMailPropertiesFileName());
            prop.load(inputStream);

            String host = prop.getProperty("host");
            String from = prop.getProperty("from");
            String pass = prop.getProperty("pass");
            isEmailPropertiesOK = (host != null && !host.equals("")) && (from != null && !from.equals("")) && (pass != null && !pass.equals(""));
            String strPort = prop.getProperty("port");
            isPortOk = (strPort != null) && (strPort.matches("[0-9]+"));

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return isEmailPropertiesOK && isPortOk;
        }


    }

}

