package com.amcbridge.jenkins.plugins.vsc;

public class VersionControlSystemResult {

    private String errorMassage;
    private Long numberOfRevision;
    private Boolean success;

    public VersionControlSystemResult(Boolean isSuccess) {
        success = isSuccess;
    }

    public VersionControlSystemResult(Boolean isSuccess, Long numberOfNewRevision) {
        success = isSuccess;
        numberOfRevision = numberOfNewRevision;
    }

    public VersionControlSystemResult(Boolean isSuccess, Long numberOfNewRevision, String errorMassage) {
        success = isSuccess;
        numberOfRevision = numberOfNewRevision;
        this.errorMassage = errorMassage;
    }

    public void setErrorMassage(String value) {
        errorMassage = value;
    }

    public String getErrorMassage() {
        return errorMassage;
    }

    public void setNumberOfRevision(Long value) {
        numberOfRevision = value;
    }

    public Long getNumberOfRevision() {
        return numberOfRevision;
    }

    public void setSuccess(Boolean value) {
        success = value;
    }

    public Boolean getSuccess() {
        return success;
    }
}
