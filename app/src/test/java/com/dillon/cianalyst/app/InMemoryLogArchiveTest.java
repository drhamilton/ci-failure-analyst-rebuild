package com.dillon.cianalyst.app;

import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.BuildLog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InMemoryLogArchiveTest {

    @Test
    void putReturnsKeyDerivedFromTheEvent() {
        InMemoryLogArchive archive = new InMemoryLogArchive();
        BuildEvent event = new BuildEvent("123", "drhamilton/ci-failure-analyst-rebuild", "main", "failure");
        BuildLog log = new BuildLog(event, "boom: tests failed");

        String key = archive.put(log);

        assertThat(key).isEqualTo("logs/drhamilton/ci-failure-analyst-rebuild/123.txt");
    }

    @Test
    void getReturnsTheStoredContent() {
        InMemoryLogArchive archive = new InMemoryLogArchive();
        BuildEvent event = new BuildEvent("123", "drhamilton/ci-failure-analyst-rebuild", "main", "failure");
    String key = archive.put(new BuildLog(event, "boom: tests failed"));
        assertThat(archive.get(key)).contains("boom: tests failed");
    }

}
