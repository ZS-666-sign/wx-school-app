package com.campustrade.platform.audit.controller;

import com.campustrade.platform.audit.service.AuditImageService;
import com.campustrade.platform.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AuditImageControllerTest {

    private final AuditImageService auditImageService = mock(AuditImageService.class);
    private final AuditImageController controller = new AuditImageController(auditImageService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditSuccessMessagesUseFriendlyChineseCopy() {
        authenticateReviewer();

        assertEquals("图片审核通过", controller.approve(1L).message());
        assertEquals("图片已驳回", controller.reject(1L, null).message());
        assertEquals("头像审核通过", controller.approveAvatar(1L).message());
        assertEquals("头像已驳回", controller.rejectAvatar(1L, null).message());
    }

    private void authenticateReviewer() {
        UserPrincipal principal = new UserPrincipal(1L, "reviewer@qq.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );
    }
}
