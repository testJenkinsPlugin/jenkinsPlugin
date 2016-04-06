package com.amcbridge.jenkins.plugins.serialization;

import com.google.common.collect.Lists;
import java.util.List;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PathToArtefacts {

    @XStreamImplicit(itemFieldName = "file")
    private List<String> files;

    public PathToArtefacts() {
        this.files = Lists.newLinkedList();
    }

    public void setFiles(List<String> value) {
        files = value;
    }

    public List<String> getFiles() {
        return files;
    }

    public void addFile(String value) {
        files.add(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Config)) {
            return false;
        }
        PathToArtefacts other = PathToArtefacts.class.cast(obj);
        return new EqualsBuilder()
                .append(this.files, other.files)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.files)
                .toHashCode();
    }
}
