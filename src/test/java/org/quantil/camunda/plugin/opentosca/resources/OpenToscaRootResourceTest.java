package org.quantil.camunda.plugin.opentosca.resources;

import jakarta.ws.rs.ClientErrorException;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnParser;
import org.junit.Test;
import org.quantil.camunda.plugin.opentosca.client.OpenToscaClient;
import org.quantil.camunda.plugin.opentosca.client.model.*;
import org.quantil.camunda.plugin.opentosca.dto.DeploymentInformation;

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
        getProcessEngine().getRuntimeService().setVariable(processInstance.getProcessInstanceId(), "Activity_1ay99v4_deploymentBuildPlanInstanceUrl", "url_from_var");
        OpenToscaRootResource openToscaRootResource = new OpenToscaRootResource();
        OpenToscaClient client = mock(OpenToscaClient.class);
        openToscaRootResource.setOpenToscaClient(client);

        BuildPlanInstance firstInstance = new BuildPlanInstance();
        firstInstance.setState("STARTED");
        firstInstance.setLogs(Collections.emptyList());
        firstInstance.setLinks(Collections.emptyMap());
        when(client.fetch("http://localhost:1337/csars/PetClinic_MySQL-OpenStack-w1.csar/servicetemplates/PetClinic_MySQL-OpenStack-w1/buildplans/PetClinic_MySQL-OpenStack-w1_buildPlan/instances/16896083708250.9122025474743877",
                BuildPlanInstance.class)).thenReturn(firstInstance);

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
        when(client.fetch("url_from_var", BuildPlanInstance.class)).thenReturn(secondInstance);

        when(client.fetch("http://localhost:1337/csars/PetClinic_MySQL-OpenStack-w1.csar/servicetemplates/PetClinic_MySQL-OpenStack-w1/buildplans/PetClinic_MySQL-OpenStack-w1_buildPlan/instances/16896083708250.91220254747438636", BuildPlanInstance.class)).thenThrow(ClientErrorException.class);


        ServiceTemplateInstance serviceTemplateInstance = new ServiceTemplateInstance();
        serviceTemplateInstance.setCreatedAt(2000L);
        serviceTemplateInstance.setState("OK");
        when(client.fetch(eq("http://service_template_instance"), any())).thenReturn(serviceTemplateInstance);


        List<DeploymentInformation> deploymentInformations = openToscaRootResource.getDeploymentInformation(getProcessEngine().getName(), processInstance.getProcessInstanceId());

        assertEquals(2, deploymentInformations.size());
        assertEquals("PetClinic_MySQL-OpenStack-w1", deploymentInformations.get(0).getCsarName());
        assertEquals("STARTED", deploymentInformations.get(0).getBuildPlanState());
        assertEquals(null, deploymentInformations.get(0).getInstanceState());
        assertEquals("abcServiceTemplate-Activity_1sfrg9c", deploymentInformations.get(1).getCsarName());
        assertEquals("PROVISIONED", deploymentInformations.get(1).getBuildPlanState());
        assertEquals("OK", deploymentInformations.get(1).getInstanceState());
        assertEquals(1, deploymentInformations.get(1).getLogs().size());
        assertEquals("Hello World", deploymentInformations.get(1).getLogs().get(0).getMessage());
        assertEquals("INFO", deploymentInformations.get(1).getLogs().get(0).getStatus());
        assertEquals(new Date(1000), deploymentInformations.get(1).getLogs().get(0).getTimestamp());
        assertEquals(new Date(2000), deploymentInformations.get(1).getInstanceCreatedAt());
    }
}
