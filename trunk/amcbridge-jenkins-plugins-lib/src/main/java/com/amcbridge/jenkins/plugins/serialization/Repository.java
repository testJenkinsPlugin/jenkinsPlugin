package com.amcbridge.jenkins.plugins.serialization;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Config)) {
            return false;
        }
        Repository other = Repository.class.cast(obj);
        return new EqualsBuilder()
                .append(this.type, other.type)
                .append(this.url, other.url)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.type)
                .append(this.url)
                .toHashCode();
    }
}
