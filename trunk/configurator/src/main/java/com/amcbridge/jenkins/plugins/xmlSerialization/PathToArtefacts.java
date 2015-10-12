package com.amcbridge.jenkins.plugins.xmlSerialization;

import java.util.ArrayList;
import java.util.List;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

public class PathToArtefacts {

    @XStreamImplicit(itemFieldName = "file")
    private List<String> files;

    public PathToArtefacts() {
        files = new ArrayList<String>();
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

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof PathToArtefacts)) {
            return false;
        }
        PathToArtefacts other = (PathToArtefacts) obj;
        return (this.files.containsAll(other.getFiles())
                && other.getFiles().containsAll(this.files));
    }
}
