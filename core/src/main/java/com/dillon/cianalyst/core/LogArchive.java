package com.dillon.cianalyst.core;

import java.util.Optional;

public interface LogArchive {
    String put(BuildLog log);
    Optional<String> get(String key);

    /** The storage key a log is archived under — shared by every adapter. */
    default String keyFor(BuildLog log) {
        return "logs/" + log.event().repo() + "/" + log.event().id() + ".txt";
    }
}
