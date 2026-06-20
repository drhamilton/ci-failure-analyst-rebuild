package com.dillon.cianalyst.app;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.dillon.cianalyst.core.BuildEvent;
import com.dillon.cianalyst.core.BuildLog;
import com.dillon.cianalyst.core.BuildLogFetcher;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GithubLogFetcher implements BuildLogFetcher {
    private final RestClient githubRestClient;

    @Override
    @Retry(name = "github")
    public BuildLog fetch(BuildEvent event) {
        String[] parts = event.repo().split("/", 2);

        JobsResponse response = githubRestClient.get()
            .uri("/repos/{owner}/{repo}/actions/runs/{runId}/jobs",
                parts[0], parts[1], event.id())
            .retrieve()
            .body(JobsResponse.class);

        return new BuildLog(event, summarize(response));
    }

    private String summarize(JobsResponse response) {
        if (response == null || response.jobs() == null) {
            return "No jobs found.";
        }

        String failures = response.jobs().stream()
            .filter(job -> "failure".equals(job.conclusion()))
            .map(this::describeFailedJob)
            .collect(Collectors.joining("\n"));
        
            return failures.isBlank() ? "No failed jobs found." : failures;
    }

    private String describeFailedJob(Job job) {
        String steps = job.steps() == null ? "(no step detail)"
            : job.steps().stream()
                .filter(step -> "failure".equals(step.conclusion()))
                .map(Step::name)
                .collect(Collectors.joining(", "));
        return "Job '" + job.name() + "' failed at: " + steps;
    }

    record JobsResponse(List<Job> jobs) {}
    record Job(String name, String conclusion, List<Step> steps) {}
    record Step(String name, String conclusion) {}
}
