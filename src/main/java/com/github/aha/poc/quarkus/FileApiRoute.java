package com.github.aha.poc.quarkus;

import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class FileApiRoute extends RouteBuilder {

	public final static String HEADER_CONTENT = "content";

    @ConfigProperty(name = "app.output.dir")
    String outputDir;

    @Override
    public void configure() throws Exception {
		log.info("Configuring Camel REST Route using directory: {}", outputDir);
        restConfiguration().component("platform-http");

        rest("/java")
            .post("/files")
            	.routeId("saveJavaToFileRoute")
                .to("direct:saveToFileViaJava");

        from("direct:saveToFileViaJava")
		    .log("Received a request to save tontent to file. Checking query param '%s'".formatted(HEADER_CONTENT))
		    .setBody(header(HEADER_CONTENT))
            .choice()
                .when(body().isNull())
                	.setBody(constant("'%s' query parameter is missing".formatted(HEADER_CONTENT)))
                    .setHeader("CamelHttpResponseCode", constant(400))
                .otherwise()
                	.choice()
                		.when(header("fileName").isNull())
                			.setProperty("generatedFileName", simple("${date:now:yyyyMMdd-HHmmss}.txt"))
                		.otherwise()
				        	.setProperty("generatedFileName", header("fileName"))
			        .end()
                    .toD("file:" + outputDir + "?fileName=${exchangeProperty.generatedFileName}&fileExist=Override")
                    .setBody(simple("A new file was stored as ${exchangeProperty.generatedFileName}"))
                    .setHeader("CamelHttpResponseCode", constant(200))
            .end();
    }
}
