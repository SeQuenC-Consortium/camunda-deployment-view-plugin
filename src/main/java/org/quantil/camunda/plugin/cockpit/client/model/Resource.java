package org.quantil.camunda.plugin.cockpit.client.model;

import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class Resource {

    public static class Link {
        private String href;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }

    @JsonProperty("_links")
    private HashMap<String, Link> links;

    public HashMap<String, Link> getLinks() {
        return links;
    }

    public void setLinks(HashMap<String, Link> links) {
        this.links = links;
    }
}
