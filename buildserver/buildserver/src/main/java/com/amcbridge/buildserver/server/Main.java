package com.amcbridge.buildserver.server;


import org.apache.commons.lang3.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) throws Exception {
        Logger.getLogger(BuildServer.class.getName()).log(Level.INFO, String.format("%n%s:\t%s%n", "STEP 1", "Start build server".toUpperCase()));
        identifyEnvUserDir();

        BuildServer.Builder builder = BuildServer.newBuilder();
        BuildServer server = builder
                .setArgs(args)
                .build();

        Logger.getLogger(BuildServer.class.getName()).log(Level.INFO, String.format("%n%s:\t%s%n", "STEP 2", "Initialize build server".toUpperCase()));
        server.init();

        Logger.getLogger(BuildServer.class.getName()).log(Level.INFO, String.format("%n%s:\t%s%n", "STEP 3", "Execute build server".toUpperCase()));
        server.execute();
    }

    private static void identifyEnvUserDir() throws BuildException {
        String dir = System.getenv(BuildServer.Builder.KEY_ENV_INS_PATH);
        if (StringUtils.isNotEmpty(dir)) {
            System.setProperty("user.dir", System.getenv(BuildServer.Builder.KEY_ENV_INS_PATH));
        } else {
            throw new BuildException(String.format("%s:\t%s%s%s%n%n", "ERROR", "You must explicitly set ", BuildServer.Builder.KEY_ENV_INS_PATH, " system variable to jar location"));
        }
    }
}
