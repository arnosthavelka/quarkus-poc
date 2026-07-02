package com.github.aha.poc.quarkus;

import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileApiRoute extends RouteBuilder {

    @ConfigProperty(name = "app.output.dir")
    String outputDir;

    @Override
    public void configure() throws Exception {
        restConfiguration().component("platform-http");

        rest("/files")
            .post("/content")
                .to("direct:saveToFile");

        from("direct:saveToFile")
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
