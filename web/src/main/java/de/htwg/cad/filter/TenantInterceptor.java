package de.htwg.cad.filter;

import de.htwg.cad.TenantContext;
import de.htwg.cad.exceptions.TenantNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

@Slf4j
@Component
public class TenantInterceptor implements WebRequestInterceptor {
    private static final String TENANT_HEADER = "X-Tenant";

    @Override
    public void preHandle(WebRequest request) {
        String tenantId = request.getHeader(TENANT_HEADER);

        if (!(((ServletWebRequest) request).getHttpMethod().toString().equals("OPTIONS"))) {
            if (StringUtils.isNotEmpty(tenantId)) {
                TenantContext.setTenantId(tenantId);
                log.info("Tenant header get: {}", tenantId);
            } else {
                log.error("Tenant header not found.");
            }
        }
    }

    @Override
    public void postHandle(WebRequest webRequest, ModelMap modelMap) {
        TenantContext.clear();
    }

    @Override
    public void afterCompletion(WebRequest webRequest, Exception e) {
        log.info("Interceptor afterCompletion method.");
    }
}
