import {renderDeploymentInformationTable} from "./deployment-information-table.js";
import {addSubprocessToggleButton} from "./deployment-subprocess-visualization.js";

const plugins = [
    {
        id: "process-instance-runtime-tab",
        pluginPoint: "cockpit.processInstance.runtime.tab",
        render: renderDeploymentInformationTable,
        properties: {
            label: "OpenTOSCA Deployment"
        },
        priority: 12,
    },
    {
        id: "process-instance-diagram-overlay",
        pluginPoint: "cockpit.processInstance.diagram.plugin",
        render: addSubprocessToggleButton,
        priority: 12,
    }
]

export default plugins;