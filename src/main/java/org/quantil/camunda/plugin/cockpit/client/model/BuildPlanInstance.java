package org.quantil.camunda.plugin.cockpit.client.model;

import java.util.List;

public class BuildPlanInstance extends Resource {
    private String state;

    private List<LogEntry> logs;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    public void setLogs(List<LogEntry> logs) {
        this.logs = logs;
    }
}
