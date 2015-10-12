package com.amcbridge.jenkins.plugins.xmlSerialization;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("repository")
public class Repository {

    @XStreamAsAttribute
    private String type;

    @XStreamAsAttribute
    private String url;

    public void setType(String value) {
        type = value;
    }

    public String getType() {
        return type;
    }

    public void setUrl(String value) {
        url = value;
    }

    public String getUrl() {
        return url;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof Repository)) {
            return false;
        }
        Repository other = (Repository) obj;
        return (this.type.equals(other.getType())
                && this.url.equals(other.getUrl()));
    }
}
