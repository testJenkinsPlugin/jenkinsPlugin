package com.amcbridge.jenkins.plugins.vsc;

import com.amcbridge.jenkins.plugins.utils.Tools;
import com.amcbridge.jenkins.plugins.xmlSerialization.ExportSettings.Settings;
import java.io.File;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitManager implements VersionControlSystem {

    private static final Logger log = Logger.getLogger(GitManager.class);
    private String localPath;
    private String remotePath;
    private Repository localRepo = null;
    private Repository remoteRepo = null;
    private Git git;
//    private String projectName;
    private String localRepositoryPath = "";
    private String branch = "origin/HEAD";

    public VersionControlSystemResult doCommit(String filePath, String url,
            String login, String password, String commitMessage) {

        VersionControlSystemResult commitResult = new VersionControlSystemResult(false);

        CredentialsProvider cp = new UsernamePasswordCredentialsProvider(login, password);

        String justFilePath = "";
        String realFilePath = "";
        String fileName = "";
        if (filePath != null) {
            justFilePath = filePath.substring(0, filePath.lastIndexOf("\\"));
            fileName = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.length());
        }
        realFilePath = justFilePath + "\\" + fileName;
        Settings configSettings = new Settings();
        String destFilePath = configSettings.getLocalGitRepoPath().substring(0, configSettings.getLocalGitRepoPath().length() - 5);
        Tools.copyConfig2LocalRepoPath(fileName, realFilePath, destFilePath);

        Tools.copyConfig2allPaths(filePath);
        try {
            localRepo = new FileRepositoryBuilder()
                    .setGitDir(new File(localRepositoryPath))
                    .build();
            git = new Git(localRepo);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        try {
            AddCommand ac = git.add();
            ac.setUpdate(false).addFilepattern(Tools.getJustFileName(filePath));
            try {
                ac.call();
            } catch (NoFilepatternException e) {
                System.out.println(e.getLocalizedMessage());
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }

            CommitCommand commit = git.commit();
            commit.setMessage(commitMessage);
            try {
                commit.call();
            } catch (NoHeadException e) {
                System.out.println(e.getLocalizedMessage());
            } catch (NoMessageException e) {
                System.out.println(e.getLocalizedMessage());
            } catch (ConcurrentRefUpdateException e) {
                System.out.println(e.getLocalizedMessage());
            } catch (WrongRepositoryStateException e) {
                System.out.println(e.getLocalizedMessage());
            }

            PushCommand pc = git.push();
            pc.setCredentialsProvider(cp)
                    .setForce(true)
                    .setPushAll();

            try {
                Iterator<PushResult> it = pc.call().iterator();
                if (it.hasNext()) {
                    System.out.println(it.next().getMessages());
                }
            } catch (InvalidRemoteException e) {
                System.out.println(e.getLocalizedMessage());
            }

            commitResult.setSuccess(true);

        } catch (GitAPIException ex) {
            commitResult.setSuccess(false);
            System.out.println(ex.getMessage());
        }

        return commitResult;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    private String changeFilePath(String filePath) {
        String realFilePath = "";
        if (filePath != null) {
            filePath = filePath.replace("\\", "/");
            for (int i = 0; i < filePath.length(); i++) {
                if (filePath.charAt(i) != '.') {
                    realFilePath += filePath.charAt(i);
                } else {
                    if (i < filePath.length() - 5) {
                        i++;
                    } else {
                        realFilePath += filePath.charAt(i);
                    }
                }
            }
        }
        return realFilePath;
    }

    private String changeFilePath2Relative(String filePath) {
        String realFilePath = "";
        Boolean isStartPointCatch = false;
        if (filePath != null) {
            filePath = filePath.replace("\\", "/");
            for (int i = 0; i < filePath.length(); i++) {
                if (filePath.charAt(i) != '.') {
                    if (isStartPointCatch) {
                        realFilePath += filePath.charAt(i);
                    }
                } else {
                    if (i < filePath.length() - 5) {
                        isStartPointCatch = true;
                        i++;
                    } else {
                        realFilePath += filePath.charAt(i);
                    }
                }
            }
        }
        return realFilePath;
    }

/*    public void setProjectName(String editedProjectName) {
        this.projectName = editedProjectName;
    }*/

    public void setLocalRepoPath(String localRepoPath) {
        localRepositoryPath = localRepoPath;
    }

    public void setBranch(String value) {
        branch = value;
    }
}
