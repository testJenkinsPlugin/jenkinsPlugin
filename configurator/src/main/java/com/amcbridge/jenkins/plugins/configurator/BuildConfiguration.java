package com.amcbridge.jenkins.plugins.configurator;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("buildConfiguration")
public class BuildConfiguration {
	
    private String name;

    public BuildConfiguration(String name)
    {
    	this.name = name;
    }
    
    public BuildConfiguration()
    {
    	name = "";
    }
    
    public String getName()
    {
        return name;
    }
    public void setName(String name) 
    {
        this.name = name;
    }
}