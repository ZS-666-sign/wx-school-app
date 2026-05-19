package com.campustrade.platform.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    @Test
    void defaultSuccessMessageUsesFriendlyChineseCopy() {
        ApiResponse<String> response = ApiResponse.ok("data");

        assertTrue(response.success());
        assertEquals("操作成功", response.message());
        assertEquals("data", response.data());
    }
}
