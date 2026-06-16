package com.dillon.cianalyst.app;

import java.util.List;

import com.google.cloud.spring.data.datastore.repository.DatastoreRepository;

public interface OutboxRepository extends DatastoreRepository<OutboxEntity, Long> {
   List<OutboxEntity> findByStatus(OutboxStatus status); 
}
