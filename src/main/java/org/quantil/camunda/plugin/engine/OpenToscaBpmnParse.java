package org.quantil.camunda.plugin.engine;


import org.camunda.bpm.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Namespace;

public class OpenToscaBpmnParse extends BpmnParse {
    public static final Namespace OPENTOSCA_NS = new Namespace("https://github.com/UST-QuAntiL/OpenTOSCA");

    /**
     * Constructor to be called by the {@link BpmnParser}.
     *
     * @param parser
     */
    public OpenToscaBpmnParse(BpmnParser parser) {
        super(parser);
    }

    @Override
    public void parseServiceTaskLike(ActivityImpl activity, String elementName, Element serviceTaskElement, Element camundaPropertiesElement, ScopeImpl scope) {
        String deploymentModelUrl = serviceTaskElement.attributeNS(OPENTOSCA_NS, "deploymentModelUrl");
        if(deploymentModelUrl != null) {
            activity.setActivityBehavior(new ServiceTaskDeploymentActivityBehavior(deploymentModelUrl));
        } else {
            super.parseServiceTaskLike(activity, elementName, serviceTaskElement, camundaPropertiesElement, scope);
        }
    }
}
