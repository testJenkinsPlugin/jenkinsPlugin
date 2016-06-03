package com.amcbridge.jenkins.plugins.configurator;

import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.models.BuilderConfigModel;
import com.amcbridge.jenkins.plugins.models.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.enums.ConfigurationState;
import com.amcbridge.jenkins.plugins.enums.FormResult;
import com.amcbridge.jenkins.plugins.enums.MessageDescription;
import com.amcbridge.jenkins.plugins.exceptions.JenkinsInstanceNotFoundException;
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

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Extension
public final class BuildConfigurator implements RootAction {

    private MailSender mail;
    private static final String VIEW_GENERATOR = "viewGenerator";
    private static final String PLUGIN_NAME = "Build Configurator";
    private static final String ICON_PATH = "/plugin/build-configurator/icons/system_config_services.png";
    private static final String DEFAULT_PAGE_URL = "buildconfigurator";

    public BuildConfigurator() {
        mail = new MailSender();
    }

    @Override
    public String getDisplayName() {
        if (User.current() == null) {
            return null;
        }
        return PLUGIN_NAME;
    }

    @Override
    public String getIconFileName() {
        if (User.current() == null) {
            return null;
        }
        return ICON_PATH;
    }

    @Override
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

        if (isArgsOk(currentConfig, newConfig)) {
            BuildConfigurationManager.save(newConfig);
            message.setDestinationAddress(getAdminEmails());
            mail.sendMail(message);
        }

        response.sendRedirect("./");
    }


    private boolean isArgsOk(BuildConfigurationModel currentBuildModel, BuildConfigurationModel newBuildModel) {
        String projectName = currentBuildModel.getProjectName();


        if (newBuildModel == null) {
            return false;
        }

        // User created new config with builder args
        if (!isCurrentUserAdministrator()) {
            boolean isConfigCompletelyNew = projectName == null || projectName.length() == 0;

            Map<UUID, BuilderConfigModel> newBuildersMap = getBuildersMap(newBuildModel);
            List<BuilderConfigModel> newBuildersList = new LinkedList<>(newBuildersMap.values());

            if (isConfigCompletelyNew) {
                for (BuilderConfigModel builderModel : newBuildersList) {
                    if (!builderModel.getBuilderArgs().equals("")) {
                        return false;
                    }
                }

            } else {
                Map<UUID, BuilderConfigModel> currentBuildersMap = getBuildersMap(currentBuildModel);

                for (BuilderConfigModel newBuilder : newBuildersList) {
                    boolean isBuilderExistInCurrentConfig = currentBuildersMap.containsKey(newBuilder.getGuid());
                    BuilderConfigModel currentBuilder = currentBuildersMap.get(newBuilder.getGuid());

                    if (isBuilderExistInCurrentConfig) {
                        if (!newBuilder.getBuilderArgs().equals(currentBuilder.getBuilderArgs()))
                            return false;
                    } else {
                        if (!newBuilder.getBuilderArgs().equals("")) {
                            return false;
                        }
                    }
                }

            }

        }

        // Action made by admin or user doesn't added/changed args
        return true;
    }

    private Map<UUID, BuilderConfigModel> getBuildersMap(BuildConfigurationModel configs) {
        Map<UUID, BuilderConfigModel> buildersMap = new HashMap<>();
        for (ProjectToBuildModel projectModel : configs.getProjectToBuild()) {
            for (BuilderConfigModel builderModel : projectModel.getBuilders()) {
                buildersMap.put(builderModel.getGuid(), builderModel);
            }
        }
        return buildersMap;
    }


    @JavaScriptMethod
    public ProjectToBuildView getView()
            throws UnsupportedEncodingException, JellyException {
        if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
            loadCreateNewBuildConfiguration();
        }

        try {
            return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                    .getProjectToBuildlView();
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @JavaScriptMethod
    public ProjectToBuildView loadViews(String projectName) //TODO: catch exceptions
            throws JellyException, IOException, JAXBException {
        BuildConfigurationModel conf = BuildConfigurationManager.load(projectName);

        if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
            loadCreateNewBuildConfiguration();
        }

        return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                .getProjectToBuildlView(conf.getProjectToBuild());
    }

    @JavaScriptMethod
    public ProjectToBuildView getUserAccessView()
            throws UnsupportedEncodingException, JellyException {
        if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
            loadCreateNewBuildConfiguration();
        }
        return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                .getUserAccessView();
    }

    @JavaScriptMethod
    public ProjectToBuildView loadUserAccessView(String projectName) throws UnsupportedEncodingException, JellyException {
                BuildConfigurationModel conf = null;
                try {
                    conf = BuildConfigurationManager.load(projectName);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
            loadCreateNewBuildConfiguration();
        }

        try {
            return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                    .getUserAccessView(conf.getUserWithAccess());
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
            return null;
        }
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
    public void setForDeletion(String name) throws AddressException,
            IOException, ParserConfigurationException, JAXBException, MessagingException {
        BuildConfigurationManager.markConfigurationForDeletion(name);
    }


    @JavaScriptMethod
    public void loadCreateNewBuildConfiguration() {
        try {
            Stapler.getCurrentRequest().getSession().setAttribute(VIEW_GENERATOR, new ViewGenerator());
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
        }
    }

    @JavaScriptMethod
    public Boolean isNameFree(String name) {
        try {
            return !BuildConfigurationManager.isNameUsing(name);
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @JavaScriptMethod
    public String getFullNameCreator(String creator) {
        User user;
        if (User.current() != null) {
            user = User.get(creator);
            return user.getFullName();
        } else {
            return "Error, user not found";
        }
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

    @JavaScriptMethod
    public static Boolean isCurrentUserHasAccess(String name) throws IOException, JAXBException {
        return BuildConfigurationManager.isCurrentUserHasAccess(name);
    }

    @JavaScriptMethod
    public static Boolean isCurrentUserAdministrator() {
        try {
            return BuildConfigurationManager.isCurrentUserAdministrator();
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @JavaScriptMethod
    public BuildConfigurationModel getConfiguration(String name) throws IOException, JAXBException {
        return BuildConfigurationManager.getConfiguration(name);
    }

    public static String getAdminEmails() {
        return BuildConfigurationManager.getAdminEmail();
    }

    public List<String> getSCM() {
        try {
            return BuildConfigurationManager.getSCM();
        } catch (JenkinsInstanceNotFoundException e) {
            return new LinkedList<>();
        }
    }

    public List<String> getNodesName() {
        try {
            return BuildConfigurationManager.getNodesName();
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    @JavaScriptMethod
    public boolean createJob(String name) {
        try {
            BuildConfigurationManager.createJob(name);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        // TODO alert user if failure !!!
    }

    @JavaScriptMethod
    public Boolean isJobCreated(String name) {
        try {
            return JobManagerGenerator.isJobExist(JobManagerGenerator.validJobName(name));
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @JavaScriptMethod
    public void deleteJob(String name)
            throws IOException, InterruptedException, ParserConfigurationException, JAXBException {
        BuildConfigurationManager.deleteJob(name);
    }


    @JavaScriptMethod
    public boolean isJenkinsEmailConfigOK() {
        Properties prop = new Properties();
        boolean isEmailPropertiesOK = false;
        boolean isPortOk = false;
        try (InputStream inputStream = new FileInputStream(MailSender.getMailPropertiesFileName())) {
            prop.load(inputStream);

            String host = prop.getProperty("host");
            String from = prop.getProperty("from");
            String pass = prop.getProperty("pass");
            isEmailPropertiesOK = (host != null && !host.equals("")) && (from != null && !from.equals("")) && (pass != null && !pass.equals(""));
            String strPort = prop.getProperty("port");
            isPortOk = (strPort != null) && (strPort.matches("[0-9]+"));
            return isEmailPropertiesOK && isPortOk;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @JavaScriptMethod
    public String getBuildConfiguratorVersion() {
        String version = this.getClass().getPackage().getImplementationVersion();
        return version;
    }



}

