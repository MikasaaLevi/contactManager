package com.smart.service;


import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject,String message,String to)
	{
		boolean bn = false;
		String from="anjalishaw2610@gmail.com";
		//set gmail host
		String host="smtp.gmail.com";
		
		//get system properties
		Properties properties = new Properties();
		
		//set properties for smtp
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		System.out.println("properties: " + properties);
		
		//1. get session object
		Session session = Session.getInstance(properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("anjalishaw2610@gmail.com","wjsuxinipvcfvibt");
                    }
                });
		//session.setDebug(true); //to see debug logs for session created
		//2.compose the message
		MimeMessage m = new MimeMessage(session);
        try {
        	//set from
			m.setFrom(new InternetAddress(from));
			//set recipient
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			m.setSubject(subject);
            //m.setText(message);
			m.setContent(message,"text/html");

            //send using Transport class
            Transport.send(m);
            System.out.println("sent successfully");
            bn = true;
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
        return bn;
	}
}
