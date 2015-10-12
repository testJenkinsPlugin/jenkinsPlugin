package com.amcbridge.jenkins.plugins.view;

public class ProjectToBuildView {

    private String html;
    private Integer viewId;

    public ProjectToBuildView() {
    }

    public ProjectToBuildView(String html, Integer viewId) {
        this.html = html;
        this.viewId = viewId;
    }

    public void setHtml(String value) {
        html = value;
    }

    public String getHtml() {
        return html;
    }

    public void setViewId(Integer value) {
        viewId = value;
    }

    public Integer getViewId() {
        return viewId;
    }
}
