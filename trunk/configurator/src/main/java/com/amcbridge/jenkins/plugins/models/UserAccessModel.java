package com.amcbridge.jenkins.plugins.models;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by OKravets on 5/10/2016.
 */
public class UserAccessModel {
    private String userName;

    @DataBoundConstructor
    public UserAccessModel(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
