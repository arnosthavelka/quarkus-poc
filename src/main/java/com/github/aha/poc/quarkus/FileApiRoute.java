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

        rest("/java")
            .post("/files")
                .to("direct:saveToFile");

        from("direct:saveToFile")
			.log("Received a request to save tontent to file. Checking query param 'content'")
	        .setBody(header("content"))
            .choice()
                .when(body().isNull())
                    .setBody(constant("'content' query parameter is missing"))
                    .setHeader("CamelHttpResponseCode", constant(400))
                .otherwise()
		        	.setProperty("generatedFileName", simple("${date:now:yyyyMMdd-HHmmss}.txt"))
		        	.setHeader("CamelOverruleFileName", exchangeProperty("generatedFileName"))
                    .toD("file:" + outputDir + "?fileName=${exchangeProperty.generatedFileName}&fileExist=Override")
                    .setBody(simple("A new file was stored as ${exchangeProperty.generatedFileName}"))
                    .setHeader("CamelHttpResponseCode", constant(200))
            .end();
    }
}
