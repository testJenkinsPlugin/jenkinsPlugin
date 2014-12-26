package com.amcbridge.jenkins.plugins.messenger;

public enum MessageDescription {
	CREATE("configuration was successfully created!"),
	CHANGE("configuration was changed."),
	APPROVE("configuration was successfully approved!"),
	REJECT("configuration was rejected by administrator. The reasons of rejection are:"),
	MARKED_FOR_DELETION("configuration was marked for deletion."),
	DELETE_PERMANENTLY("configuration was successfully deleted."),
	RESTORE("configuration was successfully restored.");
	

	private String messageDescriptionValue;

	private MessageDescription(String value)
	{ 
		this.messageDescriptionValue = value; 
	}

	@Override 
	public String toString()
	{ 
		return messageDescriptionValue; 
	} 
}
