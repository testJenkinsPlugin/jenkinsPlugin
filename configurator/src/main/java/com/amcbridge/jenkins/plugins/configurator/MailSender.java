package com.amcbridge.jenkins.plugins.configurator;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class MailSender
{
    String host, from, pass;
    Integer port;

    private static final String MAIL_PROPERTIES_FILE_NAME = "MailSender.properties";
    
    public void sendMail(String mailAddress, String textMessege) throws AddressException, MessagingException
    {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port); 
        props.put("mail.smtp.auth", "true");
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        InternetAddress to_address = new InternetAddress(mailAddress);
        message.addRecipient(Message.RecipientType.TO, to_address);
        message.setSubject("sending in a group");
        message.setText(textMessege + ", welcome to JavaMail ");
        Transport transport = session.getTransport("smtp");
        transport.connect(host, from, pass);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

    void load()
    {
        Properties prop = new Properties();
        InputStream input = null;
        try 
        {
            InputStream inputStream = MailSender.class.getResourceAsStream(MAIL_PROPERTIES_FILE_NAME);
            prop.load(inputStream);
            host = prop.getProperty("host");
            from = prop.getProperty("from");
            pass = prop.getProperty("pass");
            port = Integer.parseInt(prop.getProperty("port"));
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
        finally 
        {
            if (input != null) 
            {
                try 
                {
                    input.close();
                } 
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public MailSender()
    {
        load();
    }
}
