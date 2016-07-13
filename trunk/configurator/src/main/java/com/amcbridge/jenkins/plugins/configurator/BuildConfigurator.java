package com.amcbridge.jenkins.plugins.configurator;

import com.amcbridge.jenkins.plugins.job.WsPluginHelper;
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
import com.amcbridge.jenkins.plugins.xstreamelements.ScriptType;
import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.User;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Extension
public final class BuildConfigurator implements RootAction {

    private MailSender mail;
    private static final String VIEW_GENERATOR = "viewGenerator";
    private static final String PLUGIN_NAME = "Build Configurator";
    private static final String ICON_PATH = "/plugin/build-configurator/icons/system_config_services.png";
    private static final String DEFAULT_PAGE_URL = "buildconfigurator";
    private static final Logger LOGGER = LoggerFactory.getLogger(BuildConfigurator.class);


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

    @JavaScriptMethod
    public List<BuildConfigurationModel> getAllConfigurations() {
        try {
            return BuildConfigurationManager.loadAllConfigurations();
        } catch (Exception e) {
            LOGGER.error("Configurations list problem", e);
            return new LinkedList<>();
        }
    }

    public void doCopyConfig(final StaplerRequest request,
                             final StaplerResponse response) {
        try {
            JSONObject formAttribute = request.getSubmittedForm();
            String modelToCopyName = (String) formAttribute.get("copyConfigName");
            String modelNewName = (String) formAttribute.get("newConfigName");

            User user = User.current();
            boolean isCopyConfigOk = (modelToCopyName != null) && (!modelToCopyName.isEmpty());
            boolean isNewConfigOk = (modelNewName != null) && (!modelNewName.isEmpty());
            if (user != null || isCopyConfigOk || isNewConfigOk) {
                String username;
                username = BuildConfigurationManager.getCurrentUserID();

                BuildConfigurationModel modelToCopy = BuildConfigurationManager.load(modelToCopyName);
                modelToCopy.setCreator(username);
                modelToCopy.setProjectName(modelNewName);
                modelToCopy.setState(ConfigurationState.NEW);
                BuildConfigurationManager.save(modelToCopy);
            }
            response.sendRedirect("./");
        } catch (Exception e) {
            LOGGER.error("Copy configuration fail", e);
            try {
                response.sendRedirect("./");
            } catch (IOException e1) {
                LOGGER.error("Copy configuration fail", e1);
            }
        }
    }

    public void doCreateNewConfigurator(final StaplerRequest request,
                                        final StaplerResponse response) {

        try {
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
            boolean saveForDiff = false;

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
                    saveForDiff = true;
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
                if (isCurrentUserAdministrator() && saveForDiff) {
                    BuildConfigurationManager.saveForDiff(newConfig);
                }
                message.setDestinationAddress(getAdminEmails());
                mail.sendMail(message);
            }


        } catch (Exception e) {
            LOGGER.error("Fail creating new configuration", e);

        } finally {
            try {
                response.sendRedirect("./");
            } catch (IOException e) {
                LOGGER.error("Redirect page error", e);
            }
        }
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
                    if (!"".equals(builderModel.getBuilderArgs())) {
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
                        if (!"".equals(newBuilder.getBuilderArgs())) {
                            return false;
                        }
                    }
                }

            }

        }

        // Action made by admin or user doesn't added/changed args
        return true;
    }

    //TODO: check users
  /*  private boolean isUsersAccessChangedOk(BuildConfigurationModel modelNew, BuildConfigurationModel modelOld){
        List usersAccessNew = modelNew.getUserWithAccess();
        List usersAccessOld = modelNew.getUserWithAccess();

    }
*/
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
    public ProjectToBuildView getView() {
        try {
            if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
                loadCreateNewBuildConfiguration();
            }
            return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                    .getProjectToBuildlView();
        } catch (Exception e) {
            LOGGER.error("Error creating view", e);
            return null;
        }

    }

    @JavaScriptMethod
    public ProjectToBuildView loadViews(String projectName) {
        try {
            BuildConfigurationModel conf = BuildConfigurationManager.load(projectName);

            BuildConfigurationModel confDiff = BuildConfigurationManager.load(projectName + "/diff");

            if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
                loadCreateNewBuildConfiguration();
            }

            return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                    .getProjectToBuildlView(conf, confDiff);
        } catch (Exception e) {
            LOGGER.error("Error loading views", e);
            return null;
        }
    }

    @JavaScriptMethod
    public ProjectToBuildView getUserAccessView() {
        try {
            if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
                loadCreateNewBuildConfiguration();
            }
            return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                    .getUserAccessView();
        } catch (Exception e) {
            LOGGER.error("Error getting users with access", e);
            return null;
        }
    }

    @JavaScriptMethod
    public ProjectToBuildView loadUserAccessView(String projectName) {
        try {
            BuildConfigurationModel conf = BuildConfigurationManager.load(projectName);
            BuildConfigurationModel confDiff = BuildConfigurationManager.load(projectName + "/diff");
            if (conf == null) {
                throw new NullPointerException("Configuration not found");
            }
            if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
                loadCreateNewBuildConfiguration();
            }
            return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                    .getUserAccessView(conf, confDiff);
        } catch (Exception e) {
            LOGGER.error("Users access problem", e);
            return null;
        }

    }

    @JavaScriptMethod
    public ProjectToBuildView getBuilderView() {
        try {
            if (Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR) == null) {
                loadCreateNewBuildConfiguration();
            }

            return ((ViewGenerator) Stapler.getCurrentRequest().getSession().getAttribute(VIEW_GENERATOR))
                    .getBuilderView();
        } catch (Exception e) {
            LOGGER.error("Error getting builder", e);
            return null;
        }
    }

    @JavaScriptMethod
    public void setForDeletion(String name) {
        try {
            BuildConfigurationManager.markConfigurationForDeletion(name);
        } catch (Exception e) {
            LOGGER.error("Error setting for deletion", e);
        }
    }


    @JavaScriptMethod
    public void loadCreateNewBuildConfiguration() {
        try {
            Stapler.getCurrentRequest().getSession().setAttribute(VIEW_GENERATOR, new ViewGenerator());
        } catch (JenkinsInstanceNotFoundException e) {
            LOGGER.error("Error creating configuration", e);
        }
    }

    @JavaScriptMethod
    public Boolean isNameFree(String name) {
        try {
            return !BuildConfigurationManager.isNameUsing(name);
        } catch (JenkinsInstanceNotFoundException e) {
            LOGGER.error("Error checking existing configuration with this name", e);
            return false;
        }
    }

    @JavaScriptMethod
    public String getFullNameCreator(String creator) {
        try {
            User user;
            if (User.current() != null) {
                user = User.get(creator);
                return user.getFullName();
            } else {
                throw new NullPointerException("user not found");
            }
        } catch (Exception e) {
            LOGGER.error("Creator not found", e);
            return "Error, user not found";

        }
    }

    @JavaScriptMethod
    public void deleteConfigurationPermanently(String name) {
        try {
            BuildConfigurationManager.deleteConfigurationPermanently(name);
        } catch (Exception e) {
            LOGGER.error("Permanent deletion error", e);
        }
    }

    @JavaScriptMethod
    public void restoreConfiguration(String name) {
        try {
            BuildConfigurationManager.restoreConfiguration(name);
        } catch (Exception e) {
            LOGGER.error("Error restoring configuration", e);
        }
    }

    @JavaScriptMethod
    public static Boolean isCurrentUserHasAccess(String name) {
        try {
            return BuildConfigurationManager.isCurrentUserHasAccess(name);
        } catch (Exception e) {
            LOGGER.error("Error checking if user has access", e);
            return false;
        }
    }

    @JavaScriptMethod
    public static Boolean isCurrentUserAdministrator() {
        try {
            return BuildConfigurationManager.isCurrentUserAdministrator();
        } catch (Exception e) {
            LOGGER.error("Error checking user permissions", e);
            return false;
        }
    }

    @JavaScriptMethod
    public BuildConfigurationModel getConfiguration(String name) {
        try {
            return BuildConfigurationManager.getConfiguration(name);
        } catch (Exception e) {
            LOGGER.error("Error getting configuration", e);
            return null;
        }
    }

    @JavaScriptMethod
    public BuildConfigurationModel getDiffConfiguration(String name) {
          try {
            if (isCurrentUserAdministrator()) {
                return BuildConfigurationManager.getConfiguration(name + "/diff");
             }
            return null;
        } catch (Exception e) {
            LOGGER.error("Error getting configuration", e);
            return null;
        }
    }

    private static String getAdminEmails() {
        return BuildConfigurationManager.getAdminEmail();
    }

    public List<String> getSCM() {
        try {
            return BuildConfigurationManager.getSCM();
        } catch (Exception e) {
            LOGGER.error("Error getting emails", e);
            return new LinkedList<>();
        }
    }

    public List<String> getNodesName() {
        try {
            return BuildConfigurationManager.getNodesName();
        } catch (Exception e) {
            LOGGER.error("Error getting nodes", e);
            return new LinkedList<>();
        }
    }

    @JavaScriptMethod
    public boolean createJob(String name) {
        try {
            BuildConfigurationManager.createJob(name);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error job creating", e);
            return false;
        }
    }

    @JavaScriptMethod
    public Boolean isJobCreated(String name) {
        try {
            return JobManagerGenerator.isJobExist(JobManagerGenerator.validJobName(name));
        } catch (Exception e) {
            LOGGER.error("Error while checking job state", e);
            return false;
        }
    }

    @JavaScriptMethod
    public void deleteJob(String name) {
        try {

            BuildConfigurationManager.deleteJob(name);
        } catch (Exception e) {
            LOGGER.error("Error job deleting", e);
        }
    }


    @JavaScriptMethod
    public boolean isJenkinsEmailConfigOK() {
        Properties prop = new Properties();
        boolean isEmailPropertiesOK;
        boolean isPortOk;
        try (InputStream inputStream = new FileInputStream(MailSender.getMailPropertiesFileName())) {
            prop.load(inputStream);

            String host = prop.getProperty("host");
            String from = prop.getProperty("from");
            String pass = prop.getProperty("pass");
            isEmailPropertiesOK = (!"".equals(host)) && (!"".equals(from)) && (!"".equals(pass));
            String strPort = prop.getProperty("port");
            isPortOk = (strPort != null) && (strPort.matches("[0-9]+"));
            return isEmailPropertiesOK && isPortOk;

        } catch (Exception e) {
            LOGGER.error("Error checking email", e);
            return false;
        }
    }

    @JavaScriptMethod
    public String getBuildConfiguratorVersion() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    @JavaScriptMethod
    public List<ScriptType> getScriptTypes() {
        try {
            return BuildConfigurationManager.getScriptTypes();
        } catch (Exception e) {
            LOGGER.error("Error getting scripts", e);
            return new LinkedList<>();
        }
    }

    @JavaScriptMethod
    public boolean isWsPluginInstalled() {
        try {
            return WsPluginHelper.isWsPluginInstalled();
        } catch (Exception e) {
            LOGGER.error("Error checking if Workspace plugin installed", e);
            return true;
        }
    }

}

