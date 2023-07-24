package org.quantil.camunda.plugin;


import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class OpenToscaPluginTest extends AbstractCockpitPluginTest {
    @Test
    public void testPluginDiscovery() {
        CockpitPlugin samplePlugin = Cockpit.getRuntimeDelegate().getAppPluginRegistry().getPlugin("camunda-deployment-view-plugin");

        assertNotNull(samplePlugin);
    }
}
