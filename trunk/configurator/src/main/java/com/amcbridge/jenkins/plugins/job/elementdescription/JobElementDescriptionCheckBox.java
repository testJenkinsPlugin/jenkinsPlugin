package com.amcbridge.jenkins.plugins.job.elementdescription;

import org.w3c.dom.Document;

public interface JobElementDescriptionCheckBox extends JobElementDescription {

    void unCheck(Document doc);

    void check(Document doc);
}
