package com.amcbridge.jenkins.plugins.models;

import org.kohsuke.stapler.DataBoundConstructor;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAccessModel that = (UserAccessModel) o;
        return userName != null ? userName.equals(that.userName) : that.userName == null;

    }

    @Override
    public int hashCode() {
        return userName != null ? userName.hashCode() : 0;
    }
}
