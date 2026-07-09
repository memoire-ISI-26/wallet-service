package com.financedomain.wallet.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingEvent {
    private String eventType;
    private String msisdn;
    private String userId;
    private String userRole;
    private String sourceService;
    private Object payload;
    private Instant timestamp;
}
