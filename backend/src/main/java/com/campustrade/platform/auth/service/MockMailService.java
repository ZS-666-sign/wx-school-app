package com.campustrade.platform.auth.service;

import com.campustrade.platform.auth.enums.VerificationPurposeEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MockMailService.class);

    @Override
    public boolean sendVerificationCode(String email, String code, VerificationPurposeEnum purpose) {
        log.info("[DEV-MAIL] purpose={}, email={}, code={}", purpose, email, code);
        return false;
    }
}

