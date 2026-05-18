package com.campustrade.platform.goods.monitoring;

import com.campustrade.platform.config.AppProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GoodsListTimingFilterTest {

    @Test
    void goodsListRequestAddsResponseTimeHeader() throws Exception {
        AppProperties appProperties = new AppProperties();
        appProperties.getMonitoring().setGoodsListSlowThresholdMs(300);

        GoodsListTimingFilter filter = new GoodsListTimingFilter(appProperties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/goods");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNotNull(response.getHeader("X-Response-Time-Ms"));
        assertTrue(Long.parseLong(response.getHeader("X-Response-Time-Ms")) >= 0);
    }
}
