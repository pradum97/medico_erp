package com.techwhizer.medicalshop.mail;

import com.techwhizer.medicalshop.CustomDialog;
import com.victorlaerte.asynctask.AsyncTask;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class SendMail {
    private static String otpMessageBody(String otp) {

        return """
                  <html>
                  <head>
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <style>
                  body, html {
                    height: 100%;
                    margin: 0;
                    font-family: Arial, Helvetica, sans-serif;
                  }
                                  
                  ..hero-image {
                    background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url("photographer.jpg");
                    height: 50%;
                    background-position: center;
                    background-repeat: no-repeat;
                    background-size: cover;
                    position: relative;
                  }
                                  
                  ..hero-text {
                    text-align: center;
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    color: white;
                  }
                                  
                  ..hero-text button {
                    border: none;
                    outline: 0;
                    display: inline-block;
                    padding: 10px 25px;
                    color: black;
                    background-color: red;
                    text-align: center;
                    cursor: pointer;
                  }
                                  
                  ..hero-text button:hover {
                    background-color: #555;
                    color: white;
                  }
                  </style>
                  </head>
                  <body>
                                  
                <p style="font-size:20px">Your otp is: <strong style="font-size:20px">""" + otp + "</strong></p> </body> </html>";
    }

    public static String sendOtp(String toEmail) {
        String otp =Otp.generateOtp();
        String subject = "Medical Application";
        Session session = MailConfig.authSession();
        MimeMessage m = new MimeMessage(session);
        try {
            m.setFrom(new InternetAddress(MailConfig.FROM_EMAIL, "Forgot Password"));
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            m.setSubject(MimeUtility.encodeText(subject, "utf-8", "B"));

            m.setContent(otpMessageBody(otp), "text/html");
            Transport.send(m);
            new CustomDialog().showAlertBox("Success!", "OTP has been sent to: " + toEmail);
            return otp;

        } catch (Exception e) {
            new CustomDialog().showAlertBox("Failed!", "Invalid email address. Please enter valid email address");
            return "";
        }
    }

    public static boolean sendContactUsMail(String fullName, String email, String subject, String message) {
        //  String toMail = "help@tectsofts.in";
        String toMail = "pradumraj98@gmail.com";
        Session session = MailConfig.authSession();
        MimeMessage m = new MimeMessage(session);
        try {
            m.setFrom(new InternetAddress(MailConfig.FROM_EMAIL, subject));
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(toMail));
            m.setSubject(MimeUtility.encodeText(subject, "utf-8", "B"));

            m.setContent("<p>Name : " + fullName + "</p>" +
                    "<p>Email: " + email + "</p>" +
                    "<p>Message : " + message + "</p>", "text/html");
            Transport.send(m);
            new CustomDialog().showAlertBox("Success!", "Successfully sent.");
            return true;

        } catch (Exception e) {
            new CustomDialog().showAlertBox("Failed!", "Invalid email address. Please enter valid email address");
            return false;
        }
    }
}
