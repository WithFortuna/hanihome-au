package com.hanihome.hanihome_au_api.config;

import com.hanihome.hanihome_au_api.application.property.service.SearchPerformanceService;
import com.hanihome.hanihome_au_api.presentation.dto.PropertySearchRequest;
import com.hanihome.hanihome_au_api.presentation.dto.PropertySearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheInterceptorAspect {

    private final CacheManager cacheManager;
    private final SearchPerformanceService searchPerformanceService;

    @Around("@annotation(org.springframework.cache.annotation.Cacheable) && execution(* com.hanihome.hanihome_au_api.application.property.service.PropertySearchService.searchProperties(..))")
    public Object interceptCacheableSearch(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof PropertySearchRequest) {
            PropertySearchRequest request = (PropertySearchRequest) args[0];
            String queryHash = String.valueOf(request.hashCode());
            
            // Check if result is in cache
            Cache cache = cacheManager.getCache("propertySearch");
            boolean fromCache = false;
            
            if (cache != null) {
                fromCache = cache.get(queryHash) != null;
            }
            
            long startTime = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (result instanceof PropertySearchResponse) {
                PropertySearchResponse response = (PropertySearchResponse) result;
                searchPerformanceService.recordSearchMetrics(queryHash, executionTime, 
                                                           response.getNumberOfElements(), fromCache);
            }
            
            return result;
        }
        
        return joinPoint.proceed();
    }
}