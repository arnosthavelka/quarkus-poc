# quarkus-poc

This project uses Quarkus, the Supersonic Subatomic Java Framework -> see <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
mvn compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Creating a native executable

You can create a native executable using:

```shell script
mvn package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
mvn package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarkus-poc-0.0.1-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Test CURLs

```
curl -X POST "http://localhost:8080/java/files?content=HelloMyFriend"

curl -X POST "http://localhost:8080/xml/files?content=ObsahSouboruVxml"
curl -X POST "http://localhost:8080/xml/files?content=ObsahSouboruVxml&fileName=muj_specialni_soubor.txt"
```

## Project creation
### Command
```shell script
mvn io.quarkus.platform:quarkus-maven-plugin:3.15.1:create \
    -DprojectGroupId=com.github.aha.poc.quarkus \
    -DprojectArtifactId=quarkus-poc \
    -DprojectVersion=0.0.1-SNAPSHOT \
    -Dextensions="camel-quarkus-rest,camel-quarkus-platform-http,camel-quarkus-file"
```

### Outcome
```
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------< org.apache.maven:standalone-pom >-------------------
[INFO] Building Maven Stub Project (No POM) 1
[INFO] --------------------------------[ pom ]---------------------------------
[INFO]
[INFO] --- quarkus:3.15.1:create (default-cli) @ standalone-pom ---
[INFO] -----------
[INFO] selected extensions:
- org.apache.camel.quarkus:camel-quarkus-platform-http
- org.apache.camel.quarkus:camel-quarkus-file
- org.apache.camel.quarkus:camel-quarkus-rest

[INFO]
applying codestarts...
[INFO] >> java
>> maven
>> quarkus
>> config-properties
>> tooling-dockerfiles
>> tooling-maven-wrapper
[INFO]
-----------
[SUCCESS]  quarkus project has been successfully generated in:
--> <FILE_PATH>quarkus-poc
-----------
[INFO]
[INFO] ========================================================================================
[INFO] Your new application has been created in <FILE_PATH>\quarkus-poc
[INFO] Navigate into this directory and launch your application with mvn quarkus:dev
[INFO] Your application will be accessible on http://localhost:8080
[INFO] ========================================================================================
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.161 s
[INFO] Finished at: 2026-07-02T13:59:33+02:00
[INFO] ------------------------------------------------------------------------
```
