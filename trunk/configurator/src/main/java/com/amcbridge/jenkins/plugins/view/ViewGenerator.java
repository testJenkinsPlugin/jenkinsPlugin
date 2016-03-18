package com.amcbridge.jenkins.plugins.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;

import com.amcbridge.jenkins.plugins.xstreamElements.BuilderLoader;
import jenkins.model.Jenkins;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.XMLOutput;
import com.amcbridge.jenkins.plugins.configurationModels.BuilderConfigModel;
import com.amcbridge.jenkins.plugins.configurationModels.ProjectToBuildModel;
import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import com.amcbridge.jenkins.plugins.enums.*;

public class ViewGenerator {

    private static final String PROJECT_TO_BUILD_VIEW = "\\plugins\\build-configurator\\view\\ProjectToBuildView.xml";
    private static final String BUILDER_VIEW = "\\plugins\\build-configurator\\view\\BuilderView.xml";
    private static final String HTML_SPACE = "%20";

    private Integer id;
    private BuilderLoader builderLoader;

    public ViewGenerator() {
        id = 0;
        builderLoader = new BuilderLoader();
    }

    private ProjectToBuildView setContextForBuilderView(JellyContext jcontext)
            throws UnsupportedEncodingException, JellyException {
        ProjectToBuildView result = new ProjectToBuildView();

        String viewTemplatePath = Jenkins.getInstance().getRootDir() + BUILDER_VIEW;
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        jcontext.setVariable("divID", id);
        id++;

        jcontext.setVariable("builders", builderLoader.getBuilders());
        jcontext.setVariable("platforms", Platform.values());
        jcontext.setVariable("configuration", Configuration.values());

        result.setViewId(id);

        result.setHtml(launchScript(jcontext, viewTemplatePath));

        return result;
    }

    public ProjectToBuildView getBuilderView()
            throws UnsupportedEncodingException, JellyException {
        return setContextForBuilderView(new JellyContext());
    }

    public ProjectToBuildView getBuilderView(BuilderConfigModel config)
            throws UnsupportedEncodingException, JellyException {
        JellyContext jcontext = new JellyContext();
        jcontext.setVariable("view", config);
        return setContextForBuilderView(jcontext);
    }

    public ProjectToBuildView getProjectToBuildlView()
            throws UnsupportedEncodingException, JellyException {
        ProjectToBuildView result = new ProjectToBuildView();

        String viewTemplatePath = Jenkins.getInstance().getRootDir() + PROJECT_TO_BUILD_VIEW;
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
        jcontext.setVariable("platforms", Platform.values());
        jcontext.setVariable("configuration", Configuration.values());
        jcontext.setVariable("credentialsList", ProjectToBuildModel.getCredentialsList());
        jcontext.setVariable("view", new ProjectToBuildModel());
        jcontext.setVariable("userDefaultCredentials", ProjectToBuildModel.getUserDefaultCredentials());

        result.setHtml(launchScript(jcontext, viewTemplatePath));
        return result;
    }

    public ProjectToBuildView getProjectToBuildlView(List<ProjectToBuildModel> views)
            throws UnsupportedEncodingException, JellyException {
        ProjectToBuildView result = new ProjectToBuildView();
        if (views.size() == 0) {
            result.setHtml(BuildConfigurationManager.STRING_EMPTY);
            result.setViewId(0);
            return result;
        }

        String viewTemplatePath = Jenkins.getInstance().getRootDir() + PROJECT_TO_BUILD_VIEW;
        viewTemplatePath = viewTemplatePath.replaceAll(" ", HTML_SPACE);
        String html = "";

        JellyContext jcontext = new JellyContext();
        Vector<Integer> sourceId;

        jcontext.setVariable("generator", this);
        jcontext.setVariable("builders", builderLoader.getBuilders());
        jcontext.setVariable("platforms", Platform.values());
        jcontext.setVariable("configuration", Configuration.values());
        jcontext.setVariable("credentialsList", ProjectToBuildModel.getCredentialsList());
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
