package com.github.aha.poc.quarkus;

import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class FileApiRoute extends RouteBuilder {

    @ConfigProperty(name = "app.output.dir")
    String outputDir;

    @Override
    public void configure() throws Exception {
		log.info("Configuring Camel REST Route using directory: {}", outputDir);
        restConfiguration().component("platform-http");

        rest("/files")
            .post("/content")
                .to("direct:saveToFile");

        from("direct:saveToFile")
	        .log("Received a file save request. Checking query param 'ctnt'.")	
	        .setBody(header("ctnt"))
            .choice()
                .when(body().isNull())
                    .setBody(constant("'ctnt' query parameter is missing"))
                    .setHeader("CamelHttpResponseCode", constant(400))
                .otherwise()
                    .setHeader("CamelOverruleFileName", simple("${date:now:yyyyMMdd-HHmmss}.txt"))
                    .toD("file:" + outputDir + "?fileExist=Override")
                    .setBody(simple("A new file was stored as ${header.CamelOverruleFileName}"))
                    .setHeader("CamelHttpResponseCode", constant(200))
            .end();
    }
}
