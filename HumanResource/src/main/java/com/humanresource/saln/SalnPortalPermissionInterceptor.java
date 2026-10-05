package com.humanresource.saln;

import com.humanresource.onboarding.HrmPermissionGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
class SalnPortalPermissionInterceptor implements HandlerInterceptor {
    private static final String FEATURE="ep.saln";
    private final HrmPermissionGuard permissions;
    SalnPortalPermissionInterceptor(HrmPermissionGuard permissions){this.permissions=permissions;}
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler){
        String method=request.getMethod();String path=request.getRequestURI();HrmPermissionGuard.Action action;
        if("GET".equals(method))action=HrmPermissionGuard.Action.ACCESS;
        else if("DELETE".equals(method))action=HrmPermissionGuard.Action.DELETE;
        else if("PUT".equals(method))action=HrmPermissionGuard.Action.EDIT;
        else if(path.endsWith("/submit")||path.endsWith("/resubmit"))action=HrmPermissionGuard.Action.SUBMIT;
        else action=HrmPermissionGuard.Action.ADD;
        permissions.requireAction(request.getHeader("Authorization"),FEATURE,action);return true;
    }
}

@Configuration
class SalnWebConfiguration implements WebMvcConfigurer {
    private final SalnPortalPermissionInterceptor interceptor;
    SalnWebConfiguration(SalnPortalPermissionInterceptor interceptor){this.interceptor=interceptor;}
    @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(interceptor).addPathPatterns("/api/saln/my/**","/api/saln/my");}
}
