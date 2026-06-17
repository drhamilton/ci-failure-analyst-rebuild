package com.dillon.cianalyst.core;

public record BuildEvent(String id, String repo, String branch, String status) {}
