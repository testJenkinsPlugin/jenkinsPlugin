/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.amcbridge.jenkins.plugins.enums;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 *
 * @author Roma
 */
@XStreamAlias("scm")
public class SCMElement {
    @XStreamAsAttribute
    private String key, value;

    public String getKey(){
	return key;
    }

    public String getValue(){
	return value;
    }
}
