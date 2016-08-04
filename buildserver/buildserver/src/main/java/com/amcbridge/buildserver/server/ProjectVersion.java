package com.amcbridge.buildserver.server;

import com.amcbridge.jenkins.plugins.serialization.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectVersion {

    private String major;
    private String minor;
//    private String buildNumber;
    private String revision;
    private boolean versionFile;

    private ProjectVersion() {
    }

    public ProjectVersion(Project project) {
        major = "";
        minor = "";
//        buildNumber = System.getenv("BUILD_NUMBER");
        processVersions(project);

    }

    private void processVersions(Project project) {

        if (project.getVersionFiles().isVersionFile() && project.getVersionFiles().getFiles().size() == 1) {
            initMajorMinorVersions(project.getVersionFiles().getFiles().get(0));
            versionFile = true;
        }
        else {
            initRevision(project.getRepository().getType());
        }
        printVersion();
    }

 /*   public String GetVersionString(String fileVer) {
        String technologies = fileVer.substring(fileVer.indexOf('.') + 1);
        String separator = ".";
        if (technologies.compareToIgnoreCase("rc") == 0) {
            separator = ",";
        }

        return major + separator + minor + separator + buildNumber + separator + revision;
    }*/

    private void initRevision(String vcs) {
        if (vcs.equalsIgnoreCase("git")) {
            revision = System.getenv("GIT_COMMIT");
            revision = revision.substring(revision.length()-12);
            /*try {
                System.setProperty("user.dir", System.getenv("WORKSPACE"));
                Process p = Runtime.getRuntime().exec("git rev-list --count HEAD");
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                revision = in.readLine();
                in.close();
            } catch (Exception ex) {
                Logger.getLogger(ProjectVersion.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        } else {
            revision = System.getenv("SVN_REVISION");
        }
    }

    private boolean initMajorMinorVersions(String fileVer) {
        String technologies = fileVer.substring(fileVer.indexOf('.') + 1);
        fileVer = System.getenv("WORKSPACE") + File.separator + fileVer;
        switch (technologies) {
            case "rc":
                try {
                    File f = new File(fileVer);
                    char b[] = new char[(int) f.length()];
                    FileReader fr = new FileReader(f);
                    fr.read(b);
                    fr.close();
                    String tmp = new String(b);
                    tmp = tmp.substring(tmp.indexOf("FILEVERSION"), tmp.indexOf("PRODUCTVERSION"));
                    tmp = tmp.substring(tmp.indexOf('N') + 1);
                    tmp = tmp.replaceAll(" ", "");
                    major = tmp.substring(0, tmp.indexOf(','));
                    tmp = tmp.substring(tmp.indexOf(',') + 1, tmp.length());
                    minor = tmp.substring(0, tmp.indexOf(','));
                } catch (Exception ex) {
                    Logger.getLogger(ProjectVersion.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "cs":
                try {
                    File f = new File(fileVer);
                    char b[] = new char[(int) f.length()];
                    FileReader fr = new FileReader(f);
                    fr.read(b);
                    fr.close();
                    String tmp = new String(b);
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
                } catch (Exception ex) {
                    Logger.getLogger(ProjectVersion.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "mf":
                try {
                    File f = new File(fileVer);
                    char b[] = new char[(int) f.length()];
                    FileReader fr = new FileReader(f);
                    fr.read(b);
                    fr.close();
                    String tmp = new String(b);
                    tmp = tmp.substring(tmp.indexOf("Manifest-Version:"), tmp.indexOf('\r'));
                    tmp = tmp.replaceAll("Manifest-Version:", "");
                    tmp = tmp.replaceAll(" ", "");
                    major = tmp.substring(0, tmp.indexOf('.'));
                    tmp = tmp.substring(tmp.indexOf('.') + 1, tmp.length());
                    minor = tmp.substring(0);
                } catch (Exception ex) {
                    Logger.getLogger(ProjectVersion.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "vb":
                try {
                    File f = new File(fileVer);
                    char b[] = new char[(int) f.length()];
                    FileReader fr = new FileReader(f);
                    fr.read(b);
                    fr.close();
                    String tmp = new String(b);
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
                } catch (Exception ex) {
                    Logger.getLogger(ProjectVersion.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            default:
                System.out.println("Do not identify type of technologies!");
                break;
        }
        return true;
    }

   /* public boolean SetVersion(String fileVer, String strVer) {
        String technologies = fileVer.substring(fileVer.indexOf('.') + 1);
        fileVer = System.getenv("WORKSPACE") + File.separator + fileVer;
        switch (technologies) {
            case "rc":
                try {
                    File f = new File(fileVer);
                    char b[] = new char[(int) f.length()];
                    FileReader fr = new FileReader(f);
                    fr.read(b);
                    fr.close();
                    String tmpFile = new String(b);
                    String tmp = new String(b);
                    tmp = tmp.substring(tmp.indexOf("FILEVERSION"), tmp.indexOf("PRODUCTVERSION"));
                    tmp = tmp.substring(tmp.indexOf('N') + 1);
                    tmp = tmp.replaceAll(" ", "");
                    tmp = tmp.replaceAll("\r\n", "");
                    tmp = tmp.trim();
                    tmpFile = tmpFile.replaceAll(tmp, strVer);
                    tmpFile = tmpFile.trim();
                    FileWriter fw = new FileWriter(f);
                    fw.write(tmpFile);
                    fw.flush();
                    fw.close();
                } catch (Exception ex) {
                    Logger.getLogger(ProjectVersion.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
                break;
            case "cs":
                try {
                    File f = new File(fileVer);
                    char b[] = new char[(int) f.length()];
                    FileReader fr = new FileReader(f);
                    fr.read(b);
                    String tmp = new String(b);
                    String tmpFile = new String(b);
                    tmp = tmp.substring(tmp.indexOf("AssemblyFileVersion(\""));
                    tmp = tmp.replaceAll("AssemblyFileVersion", "");
                    tmp = tmp.replace('\"', ' ');
                    tmp = tmp.replace(')', ' ');
                    tmp = tmp.replace('(', ' ');
                    tmp = tmp.replace(']', ' ');
                    tmp = tmp.replaceAll(" ", "");
                    tmp = tmp.replaceAll("\r\n", "");
                    tmp = tmp.trim();
                    tmpFile = tmpFile.replaceAll(tmp, strVer);
                    tmpFile = tmpFile.trim();
                    FileWriter fw = new FileWriter(f);
                    fw.write(tmpFile);
                    fw.flush();
                    fw.close();
                } catch (Exception ex) {
                    Logger.getLogger(ProjectVersion.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
                break;
            case "mf":
                try {
                    File f = new File(fileVer);
                    char b[] = new char[(int) f.length()];
                    FileReader fr = new FileReader(f);
                    fr.read(b);
                    String tmp = new String(b);
                    String tmpFile = new String(b);
                    tmp = tmp.substring(tmp.indexOf("Manifest-Version:"), tmp.indexOf('\r'));
                    tmp = tmp.replaceAll("Manifest-Version:", "");
                    tmp = tmp.replaceAll(" ", "");
                    tmp = tmp.replaceAll("\r\n", "");
                    tmp = tmp.trim();
                    tmpFile = tmpFile.replaceAll(tmp, strVer);
                    tmpFile = tmpFile.trim();
                    FileWriter fw = new FileWriter(f);
                    fw.write(tmpFile);
                    fw.flush();
                    fw.close();
                } catch (Exception ex) {
                    Logger.getLogger(ProjectVersion.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
                break;
            case "vb":
                try {
                    File f = new File(fileVer);
                    char b[] = new char[(int) f.length()];
                    FileReader fr = new FileReader(f);
                    fr.read(b);
                    String tmp = new String(b);
                    String tmpFile = new String(b);
                    tmp = tmp.substring(tmp.indexOf("AssemblyFileVersion(\""));
                    tmp = tmp.replaceAll("AssemblyFileVersion", "");
                    tmp = tmp.replace('\"', ' ');
                    tmp = tmp.replace(')', ' ');
                    tmp = tmp.replace('(', ' ');
                    tmp = tmp.replace('>', ' ');
                    tmp = tmp.replaceAll(" ", "");
                    tmp = tmp.replaceAll("\r\n", "");
                    tmp = tmp.trim();
                    tmpFile = tmpFile.replaceAll(tmp, strVer);
                    tmpFile = tmpFile.trim();
                    FileWriter fw = new FileWriter(f);
                    fw.write(tmpFile);
                    fw.flush();
                    fw.close();
                } catch (Exception ex) {
                    Logger.getLogger(ProjectVersion.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
                break;
            default:
                System.out.println("Do not identify type of technologies!");
                break;
        }
        return true;
    }*/

    private void printVersion() {
        StringBuilder version = new StringBuilder("");
        if (versionFile) {
            version.append("version ").append(major).append(".").append(minor);
        } else {
            version.append("revision ").append(revision);
        }
        System.out.println("[getting of version of the build started by hudson] " + version);

    }
}
