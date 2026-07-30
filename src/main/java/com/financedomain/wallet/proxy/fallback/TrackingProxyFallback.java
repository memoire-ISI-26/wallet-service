package com.financedomain.wallet.proxy.fallback;

import com.financedomain.wallet.dto.TrackingEvent;
import com.financedomain.wallet.proxy.TrackingProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrackingProxyFallback implements TrackingProxy {

    @Override
    public ResponseEntity<?> collectEvent(TrackingEvent event, String xUserRole) {
        log.warn("[Fallback] tracking-service est indisponible. Événement de tracking de transaction ignoré : {}", event.getEventType());
        return ResponseEntity.ok().build();
    }
}
