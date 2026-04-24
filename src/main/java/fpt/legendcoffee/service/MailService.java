package fpt.legendcoffee.service;

import jakarta.mail.MessagingException;

public interface MailService {
    void sendPlainText(String to, String subject, String body);
    void sendHtml(String to, String subject, String htmlBody) throws MessagingException;
}