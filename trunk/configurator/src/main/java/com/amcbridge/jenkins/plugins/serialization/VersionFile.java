package com.amcbridge.jenkins.plugins.serialization;

import com.google.common.collect.Lists;
import java.util.List;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class VersionFile {

    @XStreamImplicit(itemFieldName = "file")
    private List<String> files;

    @XStreamAsAttribute
    private Boolean versionFile;

    public VersionFile() {
        this.files = Lists.newLinkedList();
        this.versionFile = false;
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

    public void setIsVersionFile(Boolean value) {
        versionFile = value;
    }

    public Boolean isVersionFile() {
        return versionFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Config)) {
            return false;
        }
        VersionFile other = VersionFile.class.cast(obj);
        return new EqualsBuilder()
                .append(this.files, other.files)
                .append(this.versionFile, other.versionFile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.files)
                .append(this.versionFile)
                .toHashCode();
    }
}
