package com.amcbridge.jenkins.plugins.messenger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;

public class MailSender implements Runnable {

    String host, from, pass;
    Integer port;

    private static MessageInfo message;
    private static final String MAIL_PROPERTIES_FILE_NAME = "MailSender.properties";

    public void sendMail(MessageInfo message) throws AddressException,
            MessagingException {
        this.message = message;
        Thread theard = new Thread(new MailSender());
        theard.start();
    }

    void load() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            InputStream inputStream = MailSender.class.getResourceAsStream(MAIL_PROPERTIES_FILE_NAME);
            prop.load(inputStream);
            host = prop.getProperty("host");
            from = prop.getProperty("from");
            pass = prop.getProperty("pass");
            port = Integer.parseInt(prop.getProperty("port"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public MailSender() {
        load();
    }

    public void run() {
        try {
            MessageInfo message = this.message;
            Boolean isRecepients = false;
            Properties props = System.getProperties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage mMessage = new MimeMessage(session);
            mMessage.setFrom(new InternetAddress(from));
            if (message.getCC() != null && !message.getCC().isEmpty()) {
                if (message.getCC().contains(",")) {
                    InternetAddress[] address = InternetAddress.parse(message.getCC().trim());
                    mMessage.setRecipients(Message.RecipientType.CC, address);
                } else {
                    InternetAddress to_address = new InternetAddress(message.getCC().trim());
                    mMessage.addRecipient(Message.RecipientType.CC, to_address);
                }
                isRecepients = true;
            }
            if (message.getDestinationAddress() != null && message.getDestinationAddress().contains("@")
                    && !message.getDestinationAddress().trim().contains(" ")) {
                mMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(message.getDestinationAddress()));
                isRecepients = true;
            }
            if (isRecepients) {
                mMessage.setSubject(message.getSubject(), BuildConfigurationManager.ENCODING);
                mMessage.setText(message.getMassageText(), BuildConfigurationManager.ENCODING);
                Transport transport = session.getTransport("smtp");
                transport.connect(host, from, pass);
                transport.sendMessage(mMessage, mMessage.getAllRecipients());
                transport.close();
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public static String getMailPropertiesFileName(){
        return MAIL_PROPERTIES_FILE_NAME;
    }
}
