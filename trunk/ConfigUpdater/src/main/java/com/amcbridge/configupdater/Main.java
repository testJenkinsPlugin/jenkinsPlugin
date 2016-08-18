package com.amcbridge.configupdater;


import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        ConfigurationUpdater updater = new ConfigurationUpdater();
        updater.updateConfigurations();
        JOptionPane.showMessageDialog(null, "Finished. See \"configUpdate.log\" file for details");
    }
}
