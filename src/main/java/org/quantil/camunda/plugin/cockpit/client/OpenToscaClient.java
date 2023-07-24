package org.quantil.camunda.plugin.cockpit.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import org.quantil.camunda.plugin.cockpit.client.model.BuildPlanInstances;

import java.util.List;

public class OpenToscaClient implements AutoCloseable {

    private final Client client;

    public OpenToscaClient() {
        client = ClientBuilder.newClient();
    }

    public BuildPlanInstances fetchBuildPlanInstances(String baseUrl, String csarName) {
        return fetch(baseUrl + "/csars/" + csarName + ".csar/servicetemplates/" + csarName + "/buildplans/" + csarName + "_buildPlan/instances", BuildPlanInstances.class);
    }

    public <T> T fetch(String url, Class<T> clazz) {
        return client.target(url)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(clazz);
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
