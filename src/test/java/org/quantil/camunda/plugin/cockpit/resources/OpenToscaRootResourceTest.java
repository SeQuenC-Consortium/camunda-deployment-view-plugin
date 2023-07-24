package org.quantil.camunda.plugin.cockpit.resources;

import jakarta.ws.rs.ClientErrorException;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnParser;
import org.camunda.bpm.model.xml.ModelInstance;
import org.junit.Before;
import org.junit.Test;
import org.quantil.camunda.plugin.cockpit.client.OpenToscaClient;
import org.quantil.camunda.plugin.cockpit.client.model.*;
import org.quantil.camunda.plugin.cockpit.dto.DeploymentInformation;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class OpenToscaRootResourceTest extends AbstractCockpitPluginTest {
    @Test
    public void testExtractCsarNameFromDeploymentModelUrlValidUrl() {
        String url = "{{ wineryEndpoint }}/servicetemplates/http%253A%252F%252Fquantil.org%252Fquantme%252Fpush/ServiceTemplate-Activity_0qfo5i0/?csar";
        String actualCsarName = OpenToscaRootResource.extractCsarNameFromDeploymentModelUrl(url);

        assertEquals("ServiceTemplate-Activity_0qfo5i0", actualCsarName);
    }

    @Test
    public void testExtractCsarNameFromDeploymentModelUrlInvalidUrl() {
        String url = "{{ wineryEndpoint }}/servicetemplates/http%253A%252F%252Fquantil.org%252Fquantme%252Fpush/?csar";
        assertThrows(IllegalArgumentException.class, () -> {
            OpenToscaRootResource.extractCsarNameFromDeploymentModelUrl(url);
        });
    }

    @Test
    public void testGetDeploymentInformationSmokeTest() {
        deploy(createDeploymentBuilder(),
                Collections.singletonList(new BpmnParser().parseModelFromStream(OpenToscaRootResource.class.getResourceAsStream("/example-model.bpmn"))),
                Collections.emptyList());
        ProcessInstance processInstance = getProcessEngine().getRuntimeService().startProcessInstanceByKey("testProcess");
        OpenToscaRootResource openToscaRootResource = new OpenToscaRootResource();
        OpenToscaClient client = mock(OpenToscaClient.class);
        openToscaRootResource.setOpenToscaClient(client);

        BuildPlanInstances firstInstances = new BuildPlanInstances();
        BuildPlanInstance firstInstance = new BuildPlanInstance();
        firstInstance.setState("STARTED");
        firstInstance.setLogs(Collections.emptyList());
        firstInstance.setLinks(Collections.emptyMap());
        firstInstances.setPlanInstances(Collections.singletonList(firstInstance));
        when(client.fetchBuildPlanInstances("http://host.docker.internal:1337", "ServiceTemplate-Activity_0qfo5i0")).thenReturn(firstInstances);

        BuildPlanInstances secondInstances = new BuildPlanInstances();
        BuildPlanInstance secondInstance = new BuildPlanInstance();
        secondInstance.setState("PROVISIONED");
        LogEntry logEntry = new LogEntry();
        logEntry.setStatus("INFO");
        logEntry.setStartTimestamp(1000);
        logEntry.setMessage("Hello World");
        secondInstance.setLogs(Collections.singletonList(logEntry));
        Resource.Link link = new Resource.Link();
        link.setHref("http://service_template_instance");
        secondInstance.setLinks(Collections.singletonMap("service_template_instance", link));
        secondInstances.setPlanInstances(Collections.singletonList(secondInstance));
        when(client.fetchBuildPlanInstances("http://host.docker.internal:1337", "PetClinic_MySQL-OpenStack-w1")).thenReturn(secondInstances);

        when(client.fetchBuildPlanInstances("http://host.docker.internal:1337", "test_w1-wip1")).thenThrow(ClientErrorException.class);


        ServiceTemplateInstance serviceTemplateInstance = new ServiceTemplateInstance();
        serviceTemplateInstance.setCreatedAt(2000L);
        serviceTemplateInstance.setState("OK");
        when(client.fetch(eq("http://service_template_instance"), any())).thenReturn(serviceTemplateInstance);


        List<DeploymentInformation> deploymentInformations = openToscaRootResource.getDeploymentInformation(getProcessEngine().getName(), processInstance.getProcessInstanceId());

        assertEquals(2, deploymentInformations.size());
        assertEquals("ServiceTemplate-Activity_0qfo5i0", deploymentInformations.get(0).getCsarName());
        assertEquals("STARTED", deploymentInformations.get(0).getBuildPlanState());
        assertEquals(null, deploymentInformations.get(0).getInstanceState());
        assertEquals("PetClinic_MySQL-OpenStack-w1", deploymentInformations.get(1).getCsarName());
        assertEquals("PROVISIONED", deploymentInformations.get(1).getBuildPlanState());
        assertEquals("OK", deploymentInformations.get(1).getInstanceState());
        assertEquals(1, deploymentInformations.get(1).getLogs().size());
        assertEquals("Hello World", deploymentInformations.get(1).getLogs().get(0).getMessage());
        assertEquals("INFO", deploymentInformations.get(1).getLogs().get(0).getStatus());
        assertEquals(new Date(1000), deploymentInformations.get(1).getLogs().get(0).getTimestamp());
        assertEquals(new Date(2000), deploymentInformations.get(1).getInstanceCreatedAt());
    }
}
