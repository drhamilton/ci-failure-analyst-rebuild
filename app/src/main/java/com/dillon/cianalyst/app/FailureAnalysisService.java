package com.dillon.cianalyst.app;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dillon.cianalyst.core.AnalysisResult;
import com.dillon.cianalyst.core.AnalysisResultStore;
import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.BuildLog;
import com.dillon.cianalyst.core.BuildLogFetcher;
import com.dillon.cianalyst.core.FailureAnalyzer;
// import com.dillon.cianalyst.core.Notifier;
import com.dillon.cianalyst.core.WebhookParser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FailureAnalysisService {
    private final List<WebhookParser> parsers;
    private final BuildLogFetcher logFetcher;
    private final FailureAnalyzer analyzer;
    private final AnalysisResultStore store;

    public void analyze(String provider, String payload) {
        WebhookParser parser = parsers.stream()
            .filter(p -> p.supports(provider))
            .findFirst()
            .orElseThrow(() -> new UnsupportedProviderException(provider));

        BuildEvent event = parser.parse(payload);
        BuildLog log = logFetcher.fetch(event);
        AnalysisResult result = analyzer.analyze(log);
        store.save(result);
    }
}
