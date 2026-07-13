package com.financedomain.wallet.proxy.fallback;

import com.financedomain.wallet.dto.TrackingEvent;
import com.financedomain.wallet.proxy.TrackingProxy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TrackingProxyFallback implements TrackingProxy {

    @Override
    public ResponseEntity<?> collectEvent(TrackingEvent event, String xUserRole) {
        System.err.println("[Fallback] tracking-service est indisponible. Événement de tracking de transaction ignoré : " + event.getEventType());
        // Retourne un succès fictif pour ne pas bloquer les transactions financières (dépôt, retrait, transfert)
        return ResponseEntity.ok().build();
    }
}
