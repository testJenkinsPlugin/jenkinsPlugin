package com.amcbridge.configupdater;

import org.apache.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class ConfigurationUpdater {
    private static String PACKAGE_TO_FIND = "com.amcbridge.jenkins.plugins.configurationModels.";
    private static String PACKAGE_NEW_NAME = "com.amcbridge.jenkins.plugins.models.";
    private static final Logger logger = Logger.getLogger(ConfigurationUpdater.class);
    private String workingDirectory = System.getProperty("user.dir");
    private boolean renameErrorThrowed = false;

    public void updateConfigurations() {
        renameErrorThrowed = false;
        logger.info("Updating configuration start ... ");
        List<File> fileList = new LinkedList<>();
        getFilesList(workingDirectory, fileList);
        logger.info("Package name in configurations will be changed from \"" + PACKAGE_TO_FIND + "\" to \"" + PACKAGE_NEW_NAME + "\"");
        for (File configFile : fileList) {
            updatePackageName(configFile);
        }
        if (renameErrorThrowed) {
            logger.error("Update complete with errors");
        } else {
            logger.info("Update complete success");
        }

    }

    private void updatePackageName(File configFile) {
        try {
            logger.info("Updating \"" + configFile.getAbsolutePath() + "\" file");
            Path path = Paths.get(configFile.getAbsolutePath());
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll(PACKAGE_TO_FIND, PACKAGE_NEW_NAME);
            Files.write(path, content.getBytes(charset));
            logger.info("SUCCESS!");
        } catch (Exception e) {
            logger.error("Error while updating configuration file " + configFile.getAbsolutePath(), e);
            renameErrorThrowed = true;
        }
    }

    private void getFilesList(String directoryName, List<File> files) {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile() && "config.xml".equals(file.getName())) {
                files.add(file);
                logger.info(file.getAbsolutePath() + " file added to update list");
            } else if (file.isDirectory()) {
                getFilesList(file.getAbsolutePath(), files);
            }
        }
    }
}
