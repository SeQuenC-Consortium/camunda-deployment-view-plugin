# camunda-deployment-view-plugin

Plugin for the Camunda engine to show information about OpenTOSCA deployments attached to service tasks.

## Integrate into Camunda Platform Webapp

1. Build the Camunda Cockpit plugin (tested with JDK 19 and Maven 3.6.1):
```sh
mvn clean install jar:jar
```

2. [Download](https://camunda.com/download/) the Camunda run distribution (tested with [7.19](https://downloads.camunda.cloud/release/camunda-bpm/run/7.19/))

3. Copy the plugin jar file (located in `./target/`) to the `/configuration/userlib/` folder and start the server
