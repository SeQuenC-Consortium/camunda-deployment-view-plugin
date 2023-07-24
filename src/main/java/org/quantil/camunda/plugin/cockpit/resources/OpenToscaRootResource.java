package org.quantil.camunda.plugin.cockpit.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginRootResource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.jetbrains.annotations.VisibleForTesting;
import org.quantil.camunda.plugin.cockpit.OpenToscaPlugin;
import org.quantil.camunda.plugin.cockpit.client.OpenToscaClient;
import org.quantil.camunda.plugin.cockpit.client.model.BuildPlanInstance;
import org.quantil.camunda.plugin.cockpit.client.model.BuildPlanInstances;
import org.quantil.camunda.plugin.cockpit.client.model.Resource;
import org.quantil.camunda.plugin.cockpit.client.model.ServiceTemplateInstance;
import org.quantil.camunda.plugin.cockpit.dto.DeploymentInformation;

import java.util.*;
import java.util.stream.Collectors;

@Path("plugin/" + OpenToscaPlugin.ID)
public class OpenToscaRootResource extends AbstractCockpitPluginRootResource {

    private OpenToscaClient openToscaClient;

    private static final String BASE_URL = "http://host.docker.internal:1337";

    public OpenToscaRootResource() {
        super(OpenToscaPlugin.ID);
    }

    @GET
    @Path("{engineName}/process-instance/{processInstanceId}/deployment-info")
    public ArrayList<DeploymentInformation> getDeploymentInformation(@PathParam("engineName") String engineName,
                                                                     @PathParam("processInstanceId") String processInstanceId) {
        ProcessEngine processEngine = ProcessEngines.getProcessEngine(engineName);
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        ModelInstance modelInstance = processEngine.getRepositoryService().getBpmnModelInstance(processInstance.getProcessDefinitionId());
        Collection<ModelElementInstance> serviceTasks = modelInstance.getModelElementsByType(
                modelInstance.getModel().getTypeForName("http://www.omg.org/spec/BPMN/20100524/MODEL", "serviceTask"));

        ArrayList<DeploymentInformation> deploymentInformations = new ArrayList<>();
        for (ModelElementInstance serviceTask : serviceTasks) {
            String deploymentModelUrl = serviceTask.getAttributeValueNs("https://github.com/UST-QuAntiL/OpenTOSCA", "deploymentModelUrl");
            if (deploymentModelUrl == null) continue;
            try {
                String csarName = extractCsarNameFromDeploymentModelUrl(deploymentModelUrl);
                DeploymentInformation deploymentInformation = new DeploymentInformation();
                deploymentInformation.setCsarName(csarName);
                BuildPlanInstances buildPlanInstances = getOpenToscaClient().fetchBuildPlanInstances(BASE_URL, csarName);
                if (buildPlanInstances.getPlanInstances().isEmpty()) {
                    deploymentInformation.setBuildPlanState("UNAVAILABLE");
                    deploymentInformation.setInstanceState("UNAVAILABLE");
                    deploymentInformation.setLogs(Collections.emptyList());
                } else {
                    BuildPlanInstance planInstance = buildPlanInstances.getPlanInstances().get(0);
                    deploymentInformation.setBuildPlanState(planInstance.getState());
                    deploymentInformation.setLogs(planInstance.getLogs().stream()
                            .map(logEntry -> new DeploymentInformation.LogEntry(new Date(logEntry.getStartTimestamp()), logEntry.getStatus(), logEntry.getMessage()))
                            .collect(Collectors.toList()));

                    Resource.Link instanceLink = planInstance.getLinks().get("service_template_instance");
                    if (instanceLink != null) {
                        ServiceTemplateInstance serviceTemplateInstance = getOpenToscaClient().fetch(instanceLink.getHref(), ServiceTemplateInstance.class);
                        deploymentInformation.setInstanceState(serviceTemplateInstance.getState());
                        deploymentInformation.setInstanceCreatedAt(new Date(serviceTemplateInstance.getCreatedAt()));
                    }
                }
                deploymentInformations.add(deploymentInformation);
            } catch (Exception e) {
                System.err.println("Could not retrieve deployment information for " + deploymentModelUrl);
                e.printStackTrace();
            }
        }
        return deploymentInformations;
    }

    private OpenToscaClient getOpenToscaClient() {
        if(openToscaClient == null) {
            openToscaClient = new OpenToscaClient();
        }
        return openToscaClient;
    }

    @VisibleForTesting
    void setOpenToscaClient(OpenToscaClient openToscaClient) {
        this.openToscaClient = openToscaClient;
    }

    @VisibleForTesting
    static String extractCsarNameFromDeploymentModelUrl(String deploymentModelUrl) {
        String[] deploymentModelUrlParts = deploymentModelUrl.split("/");
        if (deploymentModelUrlParts.length < 5) {
            throw new IllegalArgumentException("Not a deployment model url");
        }
        return deploymentModelUrlParts[deploymentModelUrlParts.length - 2];
    }
}
