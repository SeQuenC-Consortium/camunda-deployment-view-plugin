# camunda-deployment-view-plugin

Plugin for the Camunda engine to show information about OpenTOSCA deployments attached to service tasks.

## Integrate into Camunda Platform Webapp

1. Build the Camunda Cockpit plugin (tested with jdk 19):
```sh
mvn clean install
```

2. [Download](https://camunda.com/download/) the Camunda run distribution (tested with [7.20](https://downloads.camunda.cloud/release/camunda-bpm/run/7.20/))

3. Copy the plugin jar file (located in `./target/`) to the `/configuration/userlib/` folder and start the server

## Run using Docker
Build the docker image:
```sh
docker build -t camunda-deployment-view-plugin .
```

Run the docker image:
```sh
docker run -p 8080:8080 camunda-deployment-view-plugin
```