package com.calendario.hrnest.application.notification;

/**
 * Port (strategy) — wysyłka e-maili. Implementacje w warstwie infrastructure:
 * {@code SmtpEmailSender} (produkcyjna, przez SMTP) albo {@code LoggingEmailSender}
 * (dev/test — tylko loguje, żeby nie wymagać skonfigurowanego serwera pocztowego).
 * Wybór sterowany właściwością {@code app.mail.enabled}.
 */
public interface EmailSender {

    void send(String to, String subject, String body);
}
