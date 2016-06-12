package com.amcbridge.jenkins.plugins.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;

import com.amcbridge.jenkins.plugins.models.UserAccessModel;
import com.amcbridge.jenkins.plugins.exceptions.JenkinsInstanceNotFoundException;
import com.amcbridge.jenkins.plugins.xstreamelements.BuilderLoader;
import com.amcbridge.jenkins.plugins.xstreamelements.PlatformLoader;
import jenkins.model.Jenkins;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.XMLOutput;
import com.amcbridge.jenkins.plugins.models.BuilderConfigModel;
import com.amcbridge.jenkins.plugins.models.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.*;

public class ViewGenerator {

    private static final String PROJECT_TO_BUILD_VIEW = "/plugins/build-configurator/view/ProjectToBuildView.xml";
    private static final String BUILDER_VIEW = "/plugins/build-configurator/view/BuilderView.xml";
    private static final String HTML_SPACE = "%20";
    private static final String USERACCESS_VIEW_PATH = "/plugins/build-configurator/view/UserAccessView.xml";

    private Integer id;
    private BuilderLoader builderLoader;

    public ViewGenerator() throws JenkinsInstanceNotFoundException {
        id = 0;
        builderLoader = new BuilderLoader();
    }

    private ProjectToBuildView setContextForBuilderView(JellyContext jcontext)
            throws UnsupportedEncodingException, JellyException {
        ProjectToBuildView result = new ProjectToBuildView();

        String viewTemplatePath = null;
        try {
            viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + BUILDER_VIEW;
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
            result.setHtml("");
            return result;
        }
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        jcontext.setVariable("divID", id);
        id++;
        jcontext.setVariable("builders", builderLoader.getBuilders());

        try {
            jcontext.setVariable("platforms", (new PlatformLoader()).getPlatformList());
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
            jcontext.setVariable("platforms", null);
        }
        jcontext.setVariable("configuration", Configuration.values());
        try {
            jcontext.setVariable("isAdmin", BuildConfigurationManager.isCurrentUserAdministrator());
        } catch (JenkinsInstanceNotFoundException e) {
            jcontext.setVariable("isAdmin", false);
        }

        result.setViewId(id);

        result.setHtml(launchScript(jcontext, viewTemplatePath));

        return result;
    }

    public ProjectToBuildView getBuilderView()
            throws UnsupportedEncodingException, JellyException {
        JellyContext jcontext = new JellyContext();
        BuilderConfigModel config = new BuilderConfigModel();
        jcontext.setVariable("view", config);
        return setContextForBuilderView(jcontext);
    }

    public ProjectToBuildView getBuilderView(BuilderConfigModel config)
            throws UnsupportedEncodingException, JellyException {
        JellyContext jcontext = new JellyContext();
        jcontext.setVariable("view", config);
        return setContextForBuilderView(jcontext);
    }

    private ProjectToBuildView setContextForUserAccessView(JellyContext jcontext)
            throws UnsupportedEncodingException, JellyException {
        ProjectToBuildView result = new ProjectToBuildView();
        String viewTemplatePath = null;
        try {
            viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + USERACCESS_VIEW_PATH;
        } catch (JenkinsInstanceNotFoundException e) {
            e.printStackTrace();
            result.setHtml("");
            return result;
        }
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        result.setHtml(launchScript(jcontext, viewTemplatePath));

        return result;
    }


    public ProjectToBuildView getUserAccessView()
            throws UnsupportedEncodingException, JellyException {
        return setContextForUserAccessView(new JellyContext());
    }

    public ProjectToBuildView getUserAccessView(List<UserAccessModel> userConfig)
            throws UnsupportedEncodingException, JellyException, JenkinsInstanceNotFoundException {

        ProjectToBuildView result = new ProjectToBuildView();
        if (userConfig == null || userConfig.size() == 0) {
            result.setHtml(BuildConfigurationManager.STRING_EMPTY);
            return result;
        }

        String viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + USERACCESS_VIEW_PATH;
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        String html = "";

        JellyContext jcontext = new JellyContext();
        for (int i = 0; i < userConfig.size(); i++) {
            jcontext.setVariable("userName", userConfig.get(i).getUserName());
            html += launchScript(jcontext, viewTemplatePath);
        }
        result.setHtml(html);
        return result;
    }



    public ProjectToBuildView getProjectToBuildlView()
            throws UnsupportedEncodingException, JellyException, JenkinsInstanceNotFoundException {
        ProjectToBuildView result = new ProjectToBuildView();

        String viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + PROJECT_TO_BUILD_VIEW;
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);

        JellyContext jcontext = new JellyContext();

        Vector<Integer> sourceId = new Vector<Integer>();
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

    public ProjectToBuildView getProjectToBuildlView(List<ProjectToBuildModel> views)
            throws UnsupportedEncodingException, JellyException, JenkinsInstanceNotFoundException {
        ProjectToBuildView result = new ProjectToBuildView();
        if (views.size() == 0) {
            result.setHtml(BuildConfigurationManager.STRING_EMPTY);
            result.setViewId(0);
            return result;
        }

        String viewTemplatePath = BuildConfigurationManager.getJenkins().getRootDir() + PROJECT_TO_BUILD_VIEW;
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        String html = "";

        JellyContext jcontext = new JellyContext();
        Vector<Integer> sourceId;

        jcontext.setVariable("generator", this);
        jcontext.setVariable("builders", builderLoader.getBuilders());
        jcontext.setVariable("platforms", (new PlatformLoader()).getPlatformList());
        jcontext.setVariable("configuration", Configuration.values());
        jcontext.setVariable("credentialsList", ProjectToBuildModel.getCredentialsList());
        jcontext.setVariable("isAdmin", BuildConfigurationManager.isCurrentUserAdministrator());
        for (int i = 0; i < views.size(); i++) {
            sourceId = new Vector<Integer>();

            jcontext.setVariable("divID", id);

            for (Configuration conf : Configuration.values()) {
                sourceId.add(id);
                id++;
            }
            jcontext.setVariable("view", views.get(i));
            jcontext.setVariable("sourceId", sourceId);
            html += launchScript(jcontext, viewTemplatePath);
        }

        id++;
        result.setViewId(id);
        result.setHtml(html);
        return result;
    }



    private String launchScript(JellyContext jcontext, String viewTemplatePath)
            throws UnsupportedEncodingException, JellyException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        jcontext.runScript(new File(viewTemplatePath), XMLOutput.createXMLOutput(baos));
        return new String(baos.toByteArray(), BuildConfigurationManager.ENCODING);
    }
}
