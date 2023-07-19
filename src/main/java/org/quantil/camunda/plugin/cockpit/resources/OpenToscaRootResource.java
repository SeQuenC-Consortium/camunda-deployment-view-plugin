package org.quantil.camunda.plugin.cockpit.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import org.camunda.bpm.cockpit.plugin.resource.AbstractCockpitPluginRootResource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.xml.Model;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.quantil.camunda.plugin.cockpit.OpenToscaPlugin;
import org.quantil.camunda.plugin.cockpit.client.model.BuildPlanInstance;
import org.quantil.camunda.plugin.cockpit.client.model.BuildPlanInstances;
import org.quantil.camunda.plugin.cockpit.client.model.ServiceTemplateInstance;
import org.quantil.camunda.plugin.cockpit.dto.DeploymentInformation;

import java.util.*;
import java.util.stream.Collectors;

@Path("plugin/" + OpenToscaPlugin.ID)
public class OpenToscaRootResource extends AbstractCockpitPluginRootResource {
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
        Collection<ModelElementInstance> serviceTasks = modelInstance.getModelElementsByType(modelInstance.getModel().getTypeForName("http://www.omg.org/spec/BPMN/20100524/MODEL", "serviceTask"));
        ArrayList<DeploymentInformation> deploymentInformations = new ArrayList<>();
        for (ModelElementInstance serviceTask : serviceTasks) {
            String deploymentModelUrl = serviceTask.getAttributeValueNs("https://github.com/UST-QuAntiL/OpenTOSCA", "deploymentModelUrl");
            if (deploymentModelUrl == null) continue;
            String[] deploymentModelUrlParts = deploymentModelUrl.split("/");
            String csarName = deploymentModelUrlParts[deploymentModelUrlParts.length - 2];
            String url = "http://host.docker.internal:1337/csars/" + csarName + ".csar/servicetemplates/" + csarName + "/buildplans/" + csarName + "_buildPlan/instances";
            DeploymentInformation deploymentInformation = new DeploymentInformation();
            try {
                deploymentInformation.setCsarName(csarName);
                try (Client client = ClientBuilder.newClient()) {
                    BuildPlanInstances buildPlanInstances = client.target(url)
                            .request(MediaType.APPLICATION_JSON_TYPE)
                            .get(BuildPlanInstances.class);

                    if (buildPlanInstances.getPlanInstances().isEmpty()) {
                        deploymentInformation.setBuildPlanState("UNAVAILABLE");
                        deploymentInformation.setInstanceState("UNAVAILABLE");
                        deploymentInformation.setLogs(Collections.emptyList());
                    } else {
                        BuildPlanInstance planInstance = buildPlanInstances.getPlanInstances().get(0);
                        deploymentInformation.setBuildPlanState(planInstance.getState());
                        deploymentInformation.setLogs(planInstance.getLogs().stream()
                                .map(logEntry -> new DeploymentInformation.LogEntry(new Date(logEntry.getTimestamp()), logEntry.getType(), logEntry.getMessage()))
                                .collect(Collectors.toList()));

                        String instanceLink = buildPlanInstances.getLinks().get("service_template_instance").getHref();
                        ServiceTemplateInstance instanceResponse = client.target(instanceLink)
                                .request(MediaType.APPLICATION_JSON_TYPE)
                                .get(ServiceTemplateInstance.class);
                        deploymentInformation.setInstanceState(instanceResponse.getState());
                        deploymentInformation.setInstanceCreatedAt(new Date(instanceResponse.getCreatedAt()));
                    }
                }
                deploymentInformations.add(deploymentInformation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return deploymentInformations;
    }

}
