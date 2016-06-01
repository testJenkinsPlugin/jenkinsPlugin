package com.amcbridge.jenkins.plugins.exceptions;

import java.io.FileNotFoundException;

/**
 * Created by OKravets on 5/25/2016.
 */
public class JenkinsInstanceNotFoundException extends FileNotFoundException {
    public JenkinsInstanceNotFoundException(){
        super();
    }
    public  JenkinsInstanceNotFoundException(String message){
        super(message);
    }
}
