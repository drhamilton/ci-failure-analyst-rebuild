package com.dillon.cianalyst.app;

import org.springframework.data.annotation.Id;

import com.google.cloud.spring.data.datastore.core.mapping.Entity;

@Entity(name = "analysis_results")
public class AnalysisResultEntity {
    @Id
    Long id;
    String repo;
    String branch;
    String category;
    String rootCause;
    String summary;
}
