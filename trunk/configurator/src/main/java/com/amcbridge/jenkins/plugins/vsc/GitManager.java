package com.amcbridge.jenkins.plugins.vsc;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import java.io.File;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public class GitManager implements VersionControlSystem {

    public VersionControlSystemResult doCommit(String filePath, String url, String login, String password, String commitMessage) {
        VersionControlSystemResult commitResult = new VersionControlSystemResult(false);
        String localPath, remotePath;
        Repository localRepo = null;
        Git git;
        
        localPath = "/home/me/repos/mytest";
        remotePath = "git@github.com:me/mytestrepo.git";
        git = Git.wrap(localRepo);
        File myfile = new File(localPath + "/myfile");
        
        return  null;
    }

}
