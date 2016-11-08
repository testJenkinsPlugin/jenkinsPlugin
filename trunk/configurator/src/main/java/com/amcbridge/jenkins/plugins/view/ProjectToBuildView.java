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

    public void setHtml(String html) {
        this.html = html;
    }

    public String getHtml() {
        return html;
    }

    public void setViewId(Integer viewId) {
        this.viewId = viewId;
    }

    public Integer getViewId() {
        return viewId;
    }
}
