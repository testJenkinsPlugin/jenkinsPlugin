package com.amcbridge.jenkins.plugins.xstreamElements;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import jenkins.model.Jenkins;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleksandr on 2/3/2016.
 */
@XStreamAlias("list")
public class UserLoader {
    private static final String USERS_DEFAULT_PATH = "\\plugins\\build-configurator\\usersDefaults.xml";
    @XStreamImplicit()
    private List<User> usersList = new ArrayList<User>();

    public UserLoader() {
    }

    private void load() {
        XStream xstream = new XStream();
        xstream.processAnnotations(UserLoader.class);
        xstream.processAnnotations(User.class);
        File file = new File(Jenkins.getInstance().getRootDir() + USERS_DEFAULT_PATH);
        if (file.exists()) {
            xstream.setClassLoader(User.class.getClassLoader());
            usersList = ((UserLoader) xstream.fromXML(file)).getUsersList();
            if (usersList == null) {
                usersList = new ArrayList<User>();
            }
        }

    }


    public void updateUserDefaultCredentials(String selectedCredentials) {
        if (selectedCredentials != null && selectedCredentials != "") {
            load();
            boolean credentialsIsOK = (selectedCredentials != null) && (!selectedCredentials.equals(""));
            boolean userIsRegistered = hudson.model.User.current() != null;
            boolean userExistsInList = false;

            User newUserWithDefCredentials = null;

            if (credentialsIsOK && userIsRegistered) {
                String currentUserId = hudson.model.User.current().getId();
                for (User someUser : usersList) {
                    if (someUser.getID().equals(currentUserId)) {
                        someUser.setDefCred(selectedCredentials);
                        userExistsInList = true;
                        break;
                    }
                }
                if (!userExistsInList) {
                    newUserWithDefCredentials = new User(currentUserId, selectedCredentials);
                }
            }
            if (newUserWithDefCredentials != null) {
                usersList.add(newUserWithDefCredentials);
            }
            saveUserCredentials();
        }
    }


    private void saveUserCredentials() {

        XStream xstream = new XStream();
        xstream.processAnnotations(User.class);
        xstream.processAnnotations(UserLoader.class);
        xstream.addImplicitCollection(UserLoader.class, "usersList", User.class);

        String xml = xstream.toXML(usersList);
        PrintWriter out = null;
        try {

            out = new PrintWriter(Jenkins.getInstance().getRootDir() + USERS_DEFAULT_PATH);
            out.println(xml);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    public String getUserDefaultCredentials() {
        boolean userIsRegistered = hudson.model.User.current() != null;
        load();
        if (userIsRegistered) {
            String currentUserId = hudson.model.User.current().getId();
            for (User user : usersList) {
                if (currentUserId.equals(user.getID())) {
                    return user.getDefCred();
                }
            }
        }
        return null;
    }

    private List<User> getUsersList() {
        return usersList;
    }

}
