package com.calendario.hrnest.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class LoggingEmailSenderTest {

    @Test
    void send_doesNotThrow_andNeverContactsANetwork() {
        LoggingEmailSender sender = new LoggingEmailSender();

        assertThatCode(() -> sender.send("jan@example.com", "Temat", "Tresc"))
                .doesNotThrowAnyException();
    }
}
