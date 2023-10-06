import OpenTOSCARenderer from "./OpenTOSCARenderer.js";

export function addSubprocessToggleButton(viewer, options, {control}) {
    const canvas = viewer.get("canvas")
    new OpenTOSCARenderer(viewer.get("eventBus"), viewer.get("styles"), viewer.get("bpmnRenderer"), viewer.get("textRenderer"), canvas)
    const actionButtonElement = document.createElement("button")
    let showSubProcesses = false

    const elementRegistry = viewer.get("elementRegistry")
    const drilldownOverlayBehavior = viewer.get("drilldownOverlayBehavior")
    const subProcesses = elementRegistry
        .filter(element => element.type === "bpmn:SubProcess" && element.collapsed)
    const update = () => {
        actionButtonElement.innerText = showSubProcesses ? "Hide Deployment Sub Processes" : "Show Deployment Sub Processes"
        for (const subProcess of subProcesses) {
            if (subProcess.parent !== canvas.getRootElement()) continue;
            const newType = showSubProcesses ? "bpmn:SubProcess" : "bpmn:ServiceTask"
            if (subProcess.type !== newType) {
                canvas.removeShape(subProcess)
                subProcess.type = newType
                canvas.addShape(subProcess)
                if (showSubProcesses) {
                    drilldownOverlayBehavior.addOverlay(subProcess)
                }
            }
        }
    }
    update()
    actionButtonElement.addEventListener("click", () => {
        showSubProcesses = !showSubProcesses;
        update()
    })
    control.addAction({html: actionButtonElement})
}