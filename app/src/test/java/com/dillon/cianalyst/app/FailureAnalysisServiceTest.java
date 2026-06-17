package com.dillon.cianalyst.app;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dillon.cianalyst.core.AnalysisResult;
import com.dillon.cianalyst.core.AnalysisResultStore;
import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.BuildLog;
import com.dillon.cianalyst.core.BuildLogFetcher;
import com.dillon.cianalyst.core.FailureAnalyzer;

@ExtendWith(MockitoExtension.class)
public class FailureAnalysisServiceTest {
    @Mock GithubWebhookParser githubParser;
    @Mock BuildLogFetcher logFetcher;
    @Mock FailureAnalyzer analyzer;
    @Mock AnalysisResultStore store;

    @InjectMocks FailureAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new FailureAnalysisService(List.of(githubParser), logFetcher, analyzer, store);
    }

    @Test
    void usesParserThatSupportsProvider() {
        BuildEvent event = new BuildEvent(null, null, null, null);
        BuildLog log = new BuildLog(null, null);
        AnalysisResult result = new AnalysisResult(null, null, null, null, null);

        when(githubParser.supports("github")).thenReturn(true);
        when(githubParser.parse("payload")).thenReturn(event);
        when(logFetcher.fetch(event)).thenReturn(log);
        when(analyzer.analyze(log)).thenReturn(result);

        service.analyze("github", "payload");

        verify(store).save(result);
    }

    @Test
    void throwsWhenNoParserSupportsProvider() {
        assertThatThrownBy(() -> service.analyze("gitlab", "payload"))
            .isInstanceOf(UnsupportedProviderException.class)
            .hasMessageContaining("gitlab");
    }
}
