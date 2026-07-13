package com.financedomain.wallet.proxy;

import com.financedomain.wallet.dto.TrackingEvent;
import com.financedomain.wallet.proxy.fallback.TrackingProxyFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tracking-service", fallback = TrackingProxyFallback.class)
public interface TrackingProxy {

    @PostMapping("/tracking/event")
    ResponseEntity<?> collectEvent(
            @RequestBody TrackingEvent event,
            @RequestHeader("X-User-Role") String xUserRole
    );
}
