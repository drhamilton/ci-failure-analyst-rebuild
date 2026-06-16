package com.dillon.cianalyst.app;

import com.google.cloud.spring.data.datastore.repository.DatastoreRepository;

public interface AnalysisResultRepository extends DatastoreRepository<AnalysisResultEntity, Long> {

}
