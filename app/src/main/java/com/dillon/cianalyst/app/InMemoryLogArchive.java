package com.dillon.cianalyst.app;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.dillon.cianalyst.core.BuildLog;
import com.dillon.cianalyst.core.LogArchive;

@Profile("!aws")
@Component
public class InMemoryLogArchive implements LogArchive {
    private final Map<String, String> archiveLog = new ConcurrentHashMap<>();

    @Override
    public String put(BuildLog log) {
        String key = keyFor(log);
        this.archiveLog.put(key, log.content());
        return key;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(archiveLog.get(key));
    }
}
