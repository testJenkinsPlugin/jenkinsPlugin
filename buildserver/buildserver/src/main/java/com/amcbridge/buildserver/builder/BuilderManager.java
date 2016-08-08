package com.amcbridge.buildserver.builder;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("builders")
public class BuilderManager {

    private static final String BUILDERS_FILE = "builders.xml";
    private List<Builder> builders;

    public BuilderManager() {
        builders = new ArrayList<Builder>();
    }

    public void init() throws Exception {
        loadBuilders();
    }

    private List<Builder> getBuilders() {
        return builders;
    }

    public Builder getBuilder(String name) throws Exception {
        for (Builder build : builders) {
            if (build.getName().equals(name)) {
                return build;
            }
        }
        throw new Exception(String.format("Can not find %-2s builder", name));
    }


    private void loadBuilders() throws Exception {
        File buildersFile = new File(System.getProperty("user.dir"), BUILDERS_FILE);
        if (!buildersFile.exists()) {
            throw new Exception(String.format("Can't find \"%s\" file", BUILDERS_FILE));
        }

        XStream xstream = new XStream();
        xstream.addImplicitCollection(BuilderManager.class, "builders");
        xstream.processAnnotations(BuilderManager.class);
        builders = ((BuilderManager) xstream.fromXML(buildersFile)).getBuilders();
    }
}
