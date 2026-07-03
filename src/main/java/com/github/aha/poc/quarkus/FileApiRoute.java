package com.github.aha.poc.quarkus;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class FileApiRoute extends RouteBuilder {

	public final static String ROOT_PATH = "/files";
	public final static String HEADER_CONTENT = "content";
	public final static String HEADER_FILENAME = "fileName";

    @ConfigProperty(name = "app.output.dir")
    String outputDir;

    @Override
    public void configure() throws Exception {
		log.info("Configuring Camel REST Route using directory: {}", outputDir);
        restConfiguration().component("platform-http");

        rest("/java")
		    .post(ROOT_PATH)
            	.id("saveJavaToFileRoute")
            	.to("direct:saveToFile");

		from("direct:saveToFile")
		    .log("Received a request to save tontent to file. Checking query param '%s'".formatted(HEADER_CONTENT))
		    .setBody(header(HEADER_CONTENT))
            .choice()
                .when(body().isNull())
                	.setBody(constant("'%s' query parameter is missing".formatted(HEADER_CONTENT)))
                    .setHeader(HTTP_RESPONSE_CODE, constant(400))
                .otherwise()
                	.choice()
                		.when(header(HEADER_FILENAME).isNull())
                			.setProperty("generatedFileName", simple("${date:now:yyyyMMdd-HHmmss}.txt"))
                		.otherwise()
                			.setProperty("generatedFileName", header(HEADER_FILENAME))
			        .end()
		    .toD("file:%s?%s=${exchangeProperty.generatedFileName}&fileExist=Override".formatted(outputDir, HEADER_FILENAME))
                    .setBody(simple("A new file was stored as ${exchangeProperty.generatedFileName}"))
                    .setHeader(HTTP_RESPONSE_CODE, constant(200))
            .end();
    }
}
