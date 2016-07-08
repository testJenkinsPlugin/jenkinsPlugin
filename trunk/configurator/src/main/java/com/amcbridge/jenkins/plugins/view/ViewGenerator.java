package com.amcbridge.jenkins.plugins.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

import com.amcbridge.jenkins.plugins.models.BuildConfigurationModel;
import com.amcbridge.jenkins.plugins.models.UserAccessModel;
import com.amcbridge.jenkins.plugins.exceptions.JenkinsInstanceNotFoundException;
import com.amcbridge.jenkins.plugins.xstreamelements.BuilderLoader;
import com.amcbridge.jenkins.plugins.xstreamelements.PlatformLoader;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.XMLOutput;
import com.amcbridge.jenkins.plugins.models.BuilderConfigModel;
import com.amcbridge.jenkins.plugins.models.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewGenerator {

    private static final String PROJECT_TO_BUILD_VIEW = "/plugins/build-configurator/view/ProjectToBuildView.xml";
    private static final String BUILDER_VIEW = "/plugins/build-configurator/view/BuilderView.xml";
    private static final String HTML_SPACE = "%20";
    private static final String USER_ACCESS_VIEW_PATH = "/plugins/build-configurator/view/UserAccessView.xml";
    private static final String VIEW_STATUS_NEW = "NEW";
    private static final String VIEW_STATUS_DELETED = "DELETED";
    private static final String HTML_DIV_NAME_NORMAL_SUFFIX = "";
    private static final String HTML_DIV_NAME_DELETED_SUFFIX = "_deleted";

    private static final String JCONTEXT_SOURCE_ID = "sourceId";
    private static final String JCONTEXT_DIV_ID = "divID";
    private static final String JCONTEXT_BUILDERS = "builders";
    private static final String JCONTEXT_PLATFORMS = "platforms";
    private static final String JCONTEXT_CONFIGURATION = "configuration";
    private static final String JCONTEXT_CREDENTIALS_LIST = "credentialsList";
    private static final String JCONTEXT_VIEW = "view";
    private static final String JCONTEXT_VIEW_OLD = "view_old";
    private static final String JCONTEXT_IS_ADMIN = "isAdmin";
    private static final String JCONTEXT_VIEW_STATUS = "viewStatus";
    private static final String JCONTEXT_DEF_CRED = "userDefaultCredentials";
    private static final String JCONTEXT_OLD_BUILDERS_MODELS = "oldBuildersModels";
    private static final String JCONTEXT_PROJECT_SUFFIX = "projectSuffix";
    private static final String JCONTEXT_BUILDER_SUFFIX = "builderSuffix";
    private static final String JCONTEXT_BUILDER_STATUS = "builderStatus";

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewGenerator.class);
    private Integer id;
    private BuilderLoader builderLoader;

    public ViewGenerator() throws JenkinsInstanceNotFoundException {
        id = 0;
        builderLoader = new BuilderLoader();
    }


    public ProjectToBuildView getProjectToBuildlView()
            throws UnsupportedEncodingException, JellyException, JenkinsInstanceNotFoundException {
        ProjectToBuildView result = new ProjectToBuildView();
        String viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + PROJECT_TO_BUILD_VIEW;
        JellyContext jcontext = new JellyContext();
        List <Integer> sourceId = new LinkedList<>();

        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        jcontext.setVariable(JCONTEXT_DIV_ID, id);
        result.setViewId(id);
        for (Configuration sourceControl : Configuration.values()) {
            sourceId.add(id);
            id++;
        }

        jcontext.setVariable(JCONTEXT_SOURCE_ID, sourceId);
        jcontext.setVariable(JCONTEXT_BUILDERS, builderLoader.getBuilders());
        jcontext.setVariable(JCONTEXT_PLATFORMS, (new PlatformLoader()).getPlatformList());
        jcontext.setVariable(JCONTEXT_CONFIGURATION, Configuration.values());
        jcontext.setVariable(JCONTEXT_CREDENTIALS_LIST, ProjectToBuildModel.getCredentialsList());
        jcontext.setVariable(JCONTEXT_VIEW, new ProjectToBuildModel());
        jcontext.setVariable(JCONTEXT_DEF_CRED, BuildConfigurationManager.getDefaultCredentials());
        jcontext.setVariable(JCONTEXT_IS_ADMIN, BuildConfigurationManager.isCurrentUserAdministrator());

        result.setHtml(launchScript(jcontext, viewTemplatePath));
        return result;
    }

    public ProjectToBuildView getProjectToBuildlView(BuildConfigurationModel configurationModel, BuildConfigurationModel configurationModelCompareWith)
            throws UnsupportedEncodingException, JellyException, JenkinsInstanceNotFoundException {
        List<ProjectToBuildModel> viewsCurrentConfig = configurationModel.getProjectToBuild();
        boolean showDiffToUser = BuildConfigurationManager.isCurrentUserAdministrator() && !configurationModel.getState().equals(ConfigurationState.APPROVED) &&
                !"".equals(configurationModelCompareWith.getProjectName());
        Map<UUID, ProjectToBuildModel> projectsOldMapForProjects = createProjectsMap(configurationModelCompareWith.getProjectToBuild());

        ProjectToBuildView result = new ProjectToBuildView();
        if (viewsCurrentConfig.isEmpty()) {
            result.setHtml(BuildConfigurationManager.STRING_EMPTY);
            result.setViewId(0);
            return result;
        }


        String viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + PROJECT_TO_BUILD_VIEW;
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        StringBuilder html = new StringBuilder("");

        JellyContext jcontext = new JellyContext();
        List<Integer> sourceId;

        jcontext.setVariable("generator", this);
        jcontext.setVariable(JCONTEXT_BUILDERS, builderLoader.getBuilders());
        jcontext.setVariable(JCONTEXT_PLATFORMS, (new PlatformLoader()).getPlatformList());
        jcontext.setVariable(JCONTEXT_CONFIGURATION, Configuration.values());
        jcontext.setVariable(JCONTEXT_CREDENTIALS_LIST, ProjectToBuildModel.getCredentialsList());
        jcontext.setVariable(JCONTEXT_IS_ADMIN, BuildConfigurationManager.isCurrentUserAdministrator());
        for (ProjectToBuildModel view : viewsCurrentConfig) {
            sourceId = new LinkedList<>();
            jcontext.setVariable(JCONTEXT_DIV_ID, id);
            for (Configuration conf : Configuration.values()) {
                sourceId.add(id);
                id++;
            }
            jcontext.setVariable(JCONTEXT_VIEW_OLD, null);
            jcontext.setVariable(JCONTEXT_OLD_BUILDERS_MODELS, null);
            jcontext.setVariable(JCONTEXT_VIEW_STATUS, null);

            jcontext.setVariable(JCONTEXT_PROJECT_SUFFIX, HTML_DIV_NAME_NORMAL_SUFFIX);
            jcontext.setVariable(JCONTEXT_VIEW, view);
            jcontext.setVariable(JCONTEXT_SOURCE_ID, sourceId);
            ProjectToBuildModel projectToBuildModelOld = projectsOldMapForProjects.get(view.getGuid());
            //setting diff fields
            if (showDiffToUser && projectToBuildModelOld != null) {
                jcontext.setVariable(JCONTEXT_VIEW_OLD, projectToBuildModelOld);
                jcontext.setVariable("configState", configurationModel.getState());
                jcontext.setVariable(JCONTEXT_OLD_BUILDERS_MODELS, projectToBuildModelOld.getBuilders());

            }
            if (projectsOldMapForProjects.remove(view.getGuid()) == null) {
                jcontext.setVariable(JCONTEXT_VIEW_STATUS, VIEW_STATUS_NEW);
            }
            html.append(launchScript(jcontext, viewTemplatePath));
        }
        // Getting deleted projects
        if (showDiffToUser) {
            jcontext.setVariable(JCONTEXT_VIEW_OLD, null);
            for (Map.Entry<UUID, ProjectToBuildModel> projectEntry : projectsOldMapForProjects.entrySet()) {
                jcontext.setVariable(JCONTEXT_OLD_BUILDERS_MODELS, null);
                sourceId = new Vector<>();
                jcontext.setVariable(JCONTEXT_DIV_ID, id);
                for (Configuration conf : Configuration.values()) {
                    sourceId.add(id);
                    id++;
                }
                ProjectToBuildModel view = projectEntry.getValue();
                jcontext.setVariable(JCONTEXT_VIEW, view);
                jcontext.setVariable(JCONTEXT_VIEW_STATUS, VIEW_STATUS_DELETED);
                jcontext.setVariable(JCONTEXT_PROJECT_SUFFIX, HTML_DIV_NAME_DELETED_SUFFIX);
                jcontext.setVariable(JCONTEXT_SOURCE_ID, sourceId);
                jcontext.setVariable(JCONTEXT_OLD_BUILDERS_MODELS, view.getBuilders());
                html.append(launchScript(jcontext, viewTemplatePath));
            }
        }
        id++;
        result.setViewId(id);
        result.setHtml(html.toString());
        return result;
    }


    public ProjectToBuildView getBuilderView()
            throws UnsupportedEncodingException, JellyException {
        JellyContext jcontext = new JellyContext();
        BuilderConfigModel config = new BuilderConfigModel();
        jcontext.setVariable(JCONTEXT_VIEW, config);
        jcontext.setVariable(JCONTEXT_BUILDER_SUFFIX, HTML_DIV_NAME_NORMAL_SUFFIX);
        return new ProjectToBuildView(setContextForBuilderView(jcontext), id);
    }

    public ProjectToBuildView getBuildersView(ProjectToBuildModel config, ConfigurationState configState, List<BuilderConfigModel> oldBuildersModels)
            throws UnsupportedEncodingException, JellyException, JenkinsInstanceNotFoundException {
        List<BuilderConfigModel> currentBuildersModels = config.getBuilders();
        StringBuilder strBuilder = new StringBuilder("");
        JellyContext jcontext = new JellyContext();
        Map<UUID, BuilderConfigModel> buildersOldMap = new HashMap<>();
        boolean showDiffToUser = BuildConfigurationManager.isCurrentUserAdministrator() && !ConfigurationState.APPROVED.equals(configState);
        if (oldBuildersModels != null) {
            buildersOldMap = createBuildersMap(oldBuildersModels);
        }
        if (currentBuildersModels != null) {
            for (BuilderConfigModel builder : currentBuildersModels) {
                jcontext.setVariable(JCONTEXT_BUILDER_STATUS, null);
                jcontext.setVariable("view_old_builder", null);
                jcontext.setVariable(JCONTEXT_VIEW, builder);
                jcontext.setVariable(JCONTEXT_BUILDER_SUFFIX, HTML_DIV_NAME_NORMAL_SUFFIX);
                if (showDiffToUser) {
                    if (!buildersOldMap.containsKey(builder.getGuid())) {
                        jcontext.setVariable(JCONTEXT_BUILDER_STATUS, VIEW_STATUS_NEW);
                    } else {
                        jcontext.setVariable("view_old_builder", buildersOldMap.get(builder.getGuid()));
                        jcontext.setVariable(JCONTEXT_BUILDER_STATUS, "");
                    }
                }
                buildersOldMap.remove(builder.getGuid());
                strBuilder.append(setContextForBuilderView(jcontext));
            }
        }

        //Getting deleted builders
        if (showDiffToUser) {
            for (Map.Entry<UUID, BuilderConfigModel> entrySet: buildersOldMap.entrySet()) {
                jcontext.setVariable(JCONTEXT_VIEW, entrySet.getValue());
                jcontext.setVariable(JCONTEXT_BUILDER_STATUS, VIEW_STATUS_DELETED);
                jcontext.setVariable(JCONTEXT_BUILDER_SUFFIX, HTML_DIV_NAME_DELETED_SUFFIX);
                strBuilder.append(setContextForBuilderView(jcontext));

            }
        }
        return new ProjectToBuildView(strBuilder.toString(), id);

    }

    private String setContextForBuilderView(JellyContext jcontext)
            throws UnsupportedEncodingException, JellyException {

        String viewTemplatePath;
        try {
            viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + BUILDER_VIEW;
        } catch (JenkinsInstanceNotFoundException e) {
            LOGGER.error("Error setting context for builder", e);
            return "";
        }
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        jcontext.setVariable(JCONTEXT_DIV_ID, id);
        id++;
        jcontext.setVariable(JCONTEXT_BUILDERS, builderLoader.getBuilders());
        jcontext.setVariable(JCONTEXT_PLATFORMS, (new PlatformLoader()).getPlatformList());
        jcontext.setVariable(JCONTEXT_CONFIGURATION, Configuration.values());
        try {
            jcontext.setVariable(JCONTEXT_IS_ADMIN, BuildConfigurationManager.isCurrentUserAdministrator());
        } catch (JenkinsInstanceNotFoundException e) {
            LOGGER.error("Error detecting if user is administrator", e);
            jcontext.setVariable(JCONTEXT_IS_ADMIN, false);
        }

        return launchScript(jcontext, viewTemplatePath);
    }


    public ProjectToBuildView getUserAccessView()
            throws UnsupportedEncodingException, JellyException {
        return setContextForUserAccessView(new JellyContext());
    }


    public ProjectToBuildView getUserAccessView(BuildConfigurationModel currentConfig, BuildConfigurationModel oldConfig)
            throws UnsupportedEncodingException, JellyException, JenkinsInstanceNotFoundException {

        ProjectToBuildView result = new ProjectToBuildView();

        boolean showDiff = currentConfig != null && currentConfig.getState() != null && BuildConfigurationManager.isCurrentUserAdministrator() &&
                !currentConfig.getState().equals(ConfigurationState.APPROVED);

        if (currentConfig == null || currentConfig.getUserWithAccess() == null) {
            result.setHtml(BuildConfigurationManager.STRING_EMPTY);
            return result;
        }

        List<UserAccessModel> currentUserlist = currentConfig.getUserWithAccess();
        List<UserAccessModel> oldUserlist = new LinkedList<>();

        if (oldConfig != null && oldConfig.getUserWithAccess() != null) {
            oldUserlist = oldConfig.getUserWithAccess();
        }

        String viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + USER_ACCESS_VIEW_PATH;
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        StringBuilder html = new StringBuilder("");

        JellyContext jcontext = new JellyContext();
        for (UserAccessModel userModel : currentUserlist) {
            jcontext.setVariable("userName", userModel.getUserName());
            jcontext.setVariable("userSuffix", HTML_DIV_NAME_NORMAL_SUFFIX);
            jcontext.setVariable("userStatus", null);
            if (showDiff && !oldUserlist.remove(userModel)) {
                jcontext.setVariable("userStatus", VIEW_STATUS_NEW);
            }


            html.append(launchScript(jcontext, viewTemplatePath));
        }
        if (showDiff) {
            for (UserAccessModel userModel : oldUserlist) {
                jcontext.setVariable("userName", userModel.getUserName());
                jcontext.setVariable("userSuffix", HTML_DIV_NAME_DELETED_SUFFIX);
                jcontext.setVariable("userStatus", VIEW_STATUS_DELETED);
                html.append(launchScript(jcontext, viewTemplatePath));
            }
        }
        result.setHtml(html.toString());
        return result;
    }

    private ProjectToBuildView setContextForUserAccessView(JellyContext jcontext)
            throws UnsupportedEncodingException, JellyException {
        ProjectToBuildView result = new ProjectToBuildView();
        String viewTemplatePath;
        try {
            viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + USER_ACCESS_VIEW_PATH;
        } catch (JenkinsInstanceNotFoundException e) {
            LOGGER.error("Jenkins instance not found", e);
            result.setHtml("");
            return result;
        }
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        result.setHtml(launchScript(jcontext, viewTemplatePath));

        return result;
    }


    private Map<UUID, ProjectToBuildModel> createProjectsMap(List<ProjectToBuildModel> projectList) {
        Map<UUID, ProjectToBuildModel> projectMap = new HashMap<>();
        if (projectList == null) {
            return projectMap;
        }
        for (ProjectToBuildModel project : projectList) {
            if (project != null && project.getGuid() != null) {
                projectMap.put(project.getGuid(), project);
            }
        }

        return projectMap;
    }

    private Map<UUID, BuilderConfigModel> createBuildersMap(List<BuilderConfigModel> builderList) {
        Map<UUID, BuilderConfigModel> builderMap = new HashMap<>();
        if (builderList == null) {
            return builderMap;
        }
        for (BuilderConfigModel builder : builderList) {
            if (builder != null && builder.getGuid() != null) {
                builderMap.put(builder.getGuid(), builder);
            }
        }
        return builderMap;
    }

    private String launchScript(JellyContext jcontext, String viewTemplatePath)
            throws UnsupportedEncodingException, JellyException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jcontext.runScript(new File(viewTemplatePath), XMLOutput.createXMLOutput(baos));
        return new String(baos.toByteArray(), BuildConfigurationManager.ENCODING);
    }

}
