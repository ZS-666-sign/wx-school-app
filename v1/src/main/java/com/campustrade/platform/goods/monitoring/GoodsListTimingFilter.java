package com.campustrade.platform.goods.monitoring;

import com.campustrade.platform.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class GoodsListTimingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GoodsListTimingFilter.class);
    private static final String GOODS_LIST_PATH = "/api/v1/goods";

    private final AppProperties appProperties;

    public GoodsListTimingFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"GET".equalsIgnoreCase(request.getMethod())
                || !(request.getContextPath() + GOODS_LIST_PATH).equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startNs = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            response.setHeader("X-Response-Time-Ms", Long.toString(durationMs));

            String page = request.getParameter("page");
            String size = request.getParameter("size");
            String categoryId = request.getParameter("categoryId");
            String status = request.getParameter("status");
            boolean keywordPresent = request.getParameter("keyword") != null
                    && !request.getParameter("keyword").isBlank();

            long slowThresholdMs = appProperties.getMonitoring().getGoodsListSlowThresholdMs();
            if (durationMs >= slowThresholdMs) {
                log.warn("goods_list_slow page={} size={} categoryId={} status={} keywordPresent={} httpStatus={} durationMs={}",
                        page, size, categoryId, status, keywordPresent, response.getStatus(), durationMs);
            } else {
                log.info("goods_list_timing page={} size={} categoryId={} status={} keywordPresent={} httpStatus={} durationMs={}",
                        page, size, categoryId, status, keywordPresent, response.getStatus(), durationMs);
            }
        }
    }
}
