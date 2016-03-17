package com.amcbridge.jenkins.plugins.xstreamElements;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Created by Oleksandr on 2/3/2016.
 */
@XStreamAlias("USER")
public class User {
    @XStreamAlias("ID")
    private String ID;
    @XStreamAlias("CRED")
    private String defCred;

    public User(String ID, String defCred) {
        this.ID = ID;
        this.defCred = defCred;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDefCred() {
        return defCred;
    }

    public void setDefCred(String defCred) {
        this.defCred = defCred;
    }
}
