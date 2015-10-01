/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amcbridge.jenkins.plugins.utils;

import com.amcbridge.jenkins.plugins.vsc.GitManager;
import hudson.Util;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Roma
 */
public class Tools {
    
    private static final Logger log = Logger.getLogger(GitManager.class);
    

    public static String getJustFileName(String filePath){
        String fileName = "";
        if (filePath != null) {
            fileName = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.length());
        }
        return fileName;
    }
    
    
    
    public static void copyConfig2allPaths(String filePath){
        String justFilePath = "";
        String realFilePath = "";
        String fileName = "";
        if (filePath != null) {
            justFilePath = filePath.substring(0, filePath.lastIndexOf("\\"));
            fileName = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.length());
        }
        realFilePath = justFilePath + "\\" + fileName;            
        Tools.copyConfig2BuildServerPath(fileName, realFilePath, "BUILDER_PATH");
        Tools.copyConfig2BuildServerPath(fileName, realFilePath, "BUILD_SERVER");
        Tools.copyConfig2BuildServerPath(fileName, realFilePath, "BUILD_SERVER_DEBUG");
        
    }
    
    
    public static void copyConfig2LocalRepoPath(String fileName, String realFilePath, String destFilePath){
        File source = new File(realFilePath);
        File dest = new File(destFilePath);
        if (org.codehaus.plexus.util.FileUtils.fileExists(destFilePath + "/" + fileName)) {
            try {
                Util.deleteFile(new File(destFilePath + "/" + fileName));
                FileUtils.copyFileToDirectory(source, dest);
            } catch (IOException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
    }    

    public static void copyConfig2BuildServerPath(String fileName, String realFilePath, String envVar){
        File source = new File(realFilePath);
        if (System.getenv(envVar) == null){
           log.error(envVar + " isn't defined");
           return;
        }
        String builderPath = System.getenv(envVar);
        File dest = new File(builderPath);
        if (org.codehaus.plexus.util.FileUtils.fileExists(builderPath + "/" + fileName)) {
            try {
                Util.deleteFile(new File(builderPath + "/" + fileName));
                FileUtils.copyFileToDirectory(source, dest);
            } catch (IOException ex) {
                log.error(ex.getLocalizedMessage());
            }
        }
    }      
    
}
