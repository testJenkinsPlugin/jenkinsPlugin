package com.amcbridge.jenkins.plugins.job.ElementDescription;

import org.w3c.dom.Document;

public interface JobElementDescriptionCheckBox extends JobElementDescription {

    public void unCheck(Document doc);

    public void check(Document doc);
}
