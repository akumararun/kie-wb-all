package org.kie.guvnor.testscenario.model;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.List;

@Portable
public class TestResultMessage {

    private boolean wasSuccessful;
    private int runCount;
    private int failureCount;
    private List<Failure> failures;

    public TestResultMessage() {
    }

    public TestResultMessage(boolean wasSuccessful, int runCount, int failureCount, List<Failure> failures) {
        this.wasSuccessful = wasSuccessful;
        this.runCount = runCount;
        this.failureCount = failureCount;
        this.failures = failures;
    }

    public boolean isWasSuccessful() {
        return wasSuccessful;
    }

    public int getRunCount() {
        return runCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public List<Failure> getFailures() {
        return failures;
    }
}
