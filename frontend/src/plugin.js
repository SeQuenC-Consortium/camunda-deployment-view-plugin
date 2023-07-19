import {renderDeploymentInformationTable} from "./deployment-information-table.js";

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
]

export default plugins;