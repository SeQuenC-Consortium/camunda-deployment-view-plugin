package org.quantil.camunda.plugin.cockpit;

import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;
import org.quantil.camunda.plugin.cockpit.resources.OpenToscaRootResource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Main class of the server-side plugin
 */
public class OpenToscaPlugin extends AbstractCockpitPlugin {

    public static final String ID = "camunda-deployment-view-plugin";

    public String getId() {
        return ID;
    }

    @Override
    public Set<Class<?>> getResourceClasses() {
        HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(OpenToscaRootResource.class);
        return classes;
    }
}