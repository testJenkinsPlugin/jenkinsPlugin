package com.amcbridge.buildserver.server;

import com.amcbridge.jenkins.plugins.serialization.Project;
import org.apache.log4j.Logger;

import java.io.*;

public class ProjectVersion {

    private String major;
    private String minor;
    private String revision;
    private boolean versionFile;
    private static Logger logger = Logger.getLogger(ProjectVersion.class);

    public void processVersions(Project project) {
        major = "";
        minor = "";
        if (project.getVersionFiles().isVersionFile() && project.getVersionFiles().getFiles().size() == 1) {
            initMajorMinorVersions(project.getVersionFiles().getFiles().get(0));
            versionFile = true;
        } else {
            initRevision(project.getRepository().getType());
        }
        printVersion();
    }

    private void initRevision(String vcs) {
        if ("git".equalsIgnoreCase(vcs)) {
            revision = System.getenv("GIT_COMMIT");
            revision = revision.substring(revision.length() - 12);
        } else {
            revision = System.getenv("SVN_REVISION");
        }
    }

    private void initMajorMinorVersions(String fileVer) {
        String technologies = fileVer.substring(fileVer.indexOf('.') + 1);
        fileVer = System.getenv("WORKSPACE") + File.separator + fileVer;
        File f = new File(fileVer);
        char[] b = new char[(int) f.length()];
        try (FileReader fr = new FileReader(f)) {
            fr.read(b);
            fr.close();
        } catch (IOException ex) {
            logger.error("Version file not found");
        }
        String tmp = new String(b);
        switch (technologies) {
            case "rc":
                tmp = tmp.substring(tmp.indexOf("FILEVERSION"), tmp.indexOf("PRODUCTVERSION"));
                tmp = tmp.substring(tmp.indexOf('N') + 1);
                tmp = tmp.replaceAll(" ", "");
                major = tmp.substring(0, tmp.indexOf(','));
                tmp = tmp.substring(tmp.indexOf(',') + 1, tmp.length());
                minor = tmp.substring(0, tmp.indexOf(','));
                break;
            case "cs":
                tmp = tmp.substring(tmp.indexOf("AssemblyFileVersion(\""));
                tmp = tmp.replaceAll("AssemblyFileVersion", "");
                tmp = tmp.replace('\"', ' ');
                tmp = tmp.replace(')', ' ');
                tmp = tmp.replace('(', ' ');
                tmp = tmp.replace(']', ' ');
                tmp = tmp.replaceAll(" ", "");
                major = tmp.substring(0, tmp.indexOf('.'));
                tmp = tmp.substring(tmp.indexOf('.') + 1, tmp.length());
                minor = tmp.substring(0, tmp.indexOf('.'));
                break;
            case "mf":
                tmp = tmp.substring(tmp.indexOf("Manifest-Version:"), tmp.indexOf('\r'));
                tmp = tmp.replaceAll("Manifest-Version:", "");
                tmp = tmp.replaceAll(" ", "");
                major = tmp.substring(0, tmp.indexOf('.'));
                tmp = tmp.substring(tmp.indexOf('.') + 1, tmp.length());
                minor = tmp.substring(0);
                break;
            case "vb":
                tmp = tmp.substring(tmp.indexOf("AssemblyFileVersion(\""));
                tmp = tmp.replaceAll("AssemblyFileVersion", "");
                tmp = tmp.replace('\"', ' ');
                tmp = tmp.replace(')', ' ');
                tmp = tmp.replace('(', ' ');
                tmp = tmp.replace('>', ' ');
                tmp = tmp.replaceAll(" ", "");
                major = tmp.substring(0, tmp.indexOf('.'));
                tmp = tmp.substring(tmp.indexOf('.') + 1, tmp.length());
                minor = tmp.substring(0, tmp.indexOf('.'));
                break;
            default:
                logger.error("Unknown version file extension");
                break;
        }
    }

    private void printVersion() {
        StringBuilder version = new StringBuilder();
        if (versionFile) {
            version.append("version ").append(major).append(".").append(minor);
        } else {
            version.append("revision ").append(revision);
        }
        logger.info("[getting of version of the build started by hudson] " + version);
    }
}
