package org.quantil.camunda.plugin.cockpit.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ServiceTemplateInstance extends Resource {
    private String state;

    @JsonProperty("created_at")
    private Long createdAt;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
