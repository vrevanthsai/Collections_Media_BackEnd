package com.manga.collectionBend.service;

import com.manga.collectionBend.dto.MailBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// this file contains the template(logic) of OTP-Email
@Service
public class EmailService {

//    JavaMailSender class is responsible for sending mail
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String adminEmail;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

//    MailBody- is a record which we create to structure the mail body content
    public void sendSimpleMessage(MailBody mailBody){
//        this class used for drafting out mail before sending it and JavaMailService class only takes this class object only
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailBody.to());
        message.setFrom(adminEmail); // this value will from application.yml config file which has your/admin gmail-id
        message.setSubject(mailBody.subject());
        message.setText(mailBody.text());

//        sending our drafter mail to user
        javaMailSender.send(message);
    };
}
