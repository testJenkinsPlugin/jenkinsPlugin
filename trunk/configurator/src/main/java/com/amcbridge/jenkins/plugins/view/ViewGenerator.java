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

    private static final Logger logger = LoggerFactory.getLogger(ViewGenerator.class);
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
        Vector<Integer> sourceId = new Vector<>();

        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        jcontext.setVariable("divID", id);
        result.setViewId(id);
        for (Configuration sourceControl : Configuration.values()) {
            sourceId.add(id);
            id++;
        }

        jcontext.setVariable("sourceId", sourceId);
        jcontext.setVariable("builders", builderLoader.getBuilders());
        jcontext.setVariable("platforms", (new PlatformLoader()).getPlatformList());
        jcontext.setVariable("configuration", Configuration.values());
        jcontext.setVariable("credentialsList", ProjectToBuildModel.getCredentialsList());
        jcontext.setVariable("view", new ProjectToBuildModel());
        jcontext.setVariable("userDefaultCredentials", BuildConfigurationManager.getDefaultCredentials());
        jcontext.setVariable("isAdmin", BuildConfigurationManager.isCurrentUserAdministrator());

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
        if (viewsCurrentConfig.size() == 0) {
            result.setHtml(BuildConfigurationManager.STRING_EMPTY);
            result.setViewId(0);
            return result;
        }


        String viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + PROJECT_TO_BUILD_VIEW;
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        StringBuilder html = new StringBuilder("");

        JellyContext jcontext = new JellyContext();
        Vector<Integer> sourceId;

        jcontext.setVariable("generator", this);
        jcontext.setVariable("builders", builderLoader.getBuilders());
        jcontext.setVariable("platforms", (new PlatformLoader()).getPlatformList());
        jcontext.setVariable("configuration", Configuration.values());
        jcontext.setVariable("credentialsList", ProjectToBuildModel.getCredentialsList());
        jcontext.setVariable("isAdmin", BuildConfigurationManager.isCurrentUserAdministrator());
        for (ProjectToBuildModel view : viewsCurrentConfig) {
            sourceId = new Vector<>();
            jcontext.setVariable("divID", id);
            for (Configuration conf : Configuration.values()) {
                sourceId.add(id);
                id++;
            }
            jcontext.setVariable("view_old", null);
            jcontext.setVariable("oldBuildersModels", null);
            jcontext.setVariable("viewStatus", null);

            jcontext.setVariable("projectSuffix", HTML_DIV_NAME_NORMAL_SUFFIX);
            jcontext.setVariable("view", view);
            jcontext.setVariable("sourceId", sourceId);
            ProjectToBuildModel projectToBuildModelOld = projectsOldMapForProjects.get(view.getGuid());
            //setting diff fields
            if (showDiffToUser && projectToBuildModelOld != null) {
                jcontext.setVariable("view_old", projectToBuildModelOld);
                jcontext.setVariable("configState", configurationModel.getState());
                jcontext.setVariable("oldBuildersModels", projectToBuildModelOld.getBuilders());

            }
            if (projectsOldMapForProjects.remove(view.getGuid()) == null) {
                jcontext.setVariable("viewStatus", VIEW_STATUS_NEW);
            }
            html.append(launchScript(jcontext, viewTemplatePath));
        }
        // Getting deleted projects
        if (showDiffToUser) {
            jcontext.setVariable("view_old", null);
            for (Map.Entry<UUID, ProjectToBuildModel> projectEntry : projectsOldMapForProjects.entrySet()) {
                jcontext.setVariable("oldBuildersModels", null);
                sourceId = new Vector<>();
                jcontext.setVariable("divID", id);
                for (Configuration conf : Configuration.values()) {
                    sourceId.add(id);
                    id++;
                }
                ProjectToBuildModel view = projectEntry.getValue();
                jcontext.setVariable("view", view);
                jcontext.setVariable("viewStatus", VIEW_STATUS_DELETED);
                jcontext.setVariable("projectSuffix", HTML_DIV_NAME_DELETED_SUFFIX);
                jcontext.setVariable("sourceId", sourceId);
                jcontext.setVariable("oldBuildersModels", view.getBuilders());
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
        jcontext.setVariable("view", config);
        jcontext.setVariable("builderSuffix", HTML_DIV_NAME_NORMAL_SUFFIX);
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
                jcontext.setVariable("builderStatus", null);
                jcontext.setVariable("view_old_builder", null);
                jcontext.setVariable("view", builder);
                jcontext.setVariable("builderSuffix", HTML_DIV_NAME_NORMAL_SUFFIX);
                if (showDiffToUser) {
                    if (!buildersOldMap.containsKey(builder.getGuid())) {
                        jcontext.setVariable("builderStatus", VIEW_STATUS_NEW);
                    } else {
                        jcontext.setVariable("view_old_builder", buildersOldMap.get(builder.getGuid()));
                        jcontext.setVariable("builderStatus", "");
                    }
                }
                buildersOldMap.remove(builder.getGuid());
                strBuilder.append(setContextForBuilderView(jcontext));
            }
        }

        //Getting deleted builders
        if (showDiffToUser) {
            for (Map.Entry<UUID, BuilderConfigModel> entrySet: buildersOldMap.entrySet()) {
                jcontext.setVariable("view", entrySet.getValue());
                jcontext.setVariable("builderStatus", VIEW_STATUS_DELETED);
                jcontext.setVariable("builderSuffix", HTML_DIV_NAME_DELETED_SUFFIX);
                strBuilder.append(setContextForBuilderView(jcontext));

            }
        }
        return (new ProjectToBuildView(strBuilder.toString(), id));

    }

    private String setContextForBuilderView(JellyContext jcontext)
            throws UnsupportedEncodingException, JellyException {

        String viewTemplatePath;
        try {
            viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + BUILDER_VIEW;
        } catch (JenkinsInstanceNotFoundException e) {
            logger.error("Error setting context for builder", e);
            return "";
        }
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        jcontext.setVariable("divID", id);
        id++;
        jcontext.setVariable("builders", builderLoader.getBuilders());
        jcontext.setVariable("platforms", (new PlatformLoader()).getPlatformList());
        jcontext.setVariable("configuration", Configuration.values());
        try {
            jcontext.setVariable("isAdmin", BuildConfigurationManager.isCurrentUserAdministrator());
        } catch (JenkinsInstanceNotFoundException e) {
            jcontext.setVariable("isAdmin", false);
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
            logger.error("Jenkins instance not found", e);
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
