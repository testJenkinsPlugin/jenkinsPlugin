package com.amcbridge.jenkins.plugins.exceptions;

import java.io.FileNotFoundException;

public class JenkinsInstanceNotFoundException extends FileNotFoundException {

    public JenkinsInstanceNotFoundException(){
        super();
    }

    public  JenkinsInstanceNotFoundException(String message){
        super(message);
    }
}
