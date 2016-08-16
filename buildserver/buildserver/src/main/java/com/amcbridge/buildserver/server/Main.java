package com.amcbridge.buildserver.server;

import org.apache.commons.lang3.StringUtils;

public class Main {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Main.class);
    private static final String FORMAT_STRING = "%n%s:\t%s%n";

    public static void main(String[] args) throws Exception {
        logger.info(String.format(FORMAT_STRING, "STEP 1", "Start build server".toUpperCase()));
        identifyEnvUserDir();
        BuildServer.Builder builder = BuildServer.newBuilder();
        BuildServer server = builder.setArgs(args).build();
        logger.info(String.format(FORMAT_STRING, "STEP 2", "Initialize build server".toUpperCase()));
        server.init();
        logger.info(String.format(FORMAT_STRING, "STEP 3", "Execute build server".toUpperCase()));
        server.execute();
    }

    private static void identifyEnvUserDir() throws BuildException {
        String dir = System.getenv(BuildServer.Builder.KEY_ENV_INS_PATH);
        if (StringUtils.isNotEmpty(dir)) {
            System.setProperty("user.dir", System.getenv(BuildServer.Builder.KEY_ENV_INS_PATH));
        } else {
            String message = String.format("%s:\t%s%s%s%n%n", "ERROR", "You must explicitly set ", BuildServer.Builder.KEY_ENV_INS_PATH, " system variable to jar location");
            BuildException exception = new BuildException(message);
            logger.error(message, exception);
            throw exception;
        }
    }
}
