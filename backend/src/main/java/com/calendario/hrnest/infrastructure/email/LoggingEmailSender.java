package com.calendario.hrnest.infrastructure.email;

import com.calendario.hrnest.application.notification.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Domyślna implementacja {@link EmailSender} — dev/test. Nie wymaga
 * skonfigurowanego serwera SMTP; zamiast wysyłki tylko loguje treść, żeby
 * środowisko bez `app.mail.enabled=true` (domyślnie: każde, dopóki ktoś
 * świadomie nie skonfiguruje SMTP w prod) nie wysyłało realnych maili.
 */
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(String to, String subject, String body) {
        log.info("[app.mail.enabled=false] Pomijam wysyłkę e-maila do {} — temat: \"{}\", treść: \"{}\"",
                to, subject, body);
    }
}
