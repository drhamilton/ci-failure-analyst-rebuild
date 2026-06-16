package com.dillon.cianalyst.app;

import java.time.Instant;

import org.springframework.data.annotation.Id;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;

@Entity(name = "outbox")
public class OutboxEntity {
    @Id
    Long id;
    Long referenceId;
    OutboxStatus status;
    Instant createdAt;
    Integer attempts;
}
