package com.github.aha.poc.quarkus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@Slf4j
public class JavaConfigRouteTest {

    @ConfigProperty(name = "app.output.dir")
    String outputDir;

    private Path outputPath;

    @BeforeEach
    public void setup() {
        outputPath = Paths.get(outputDir);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        if (Files.exists(outputPath)) {
            try (Stream<Path> walk = Files.walk(outputPath)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
							log.error("Log cleaning failed!", e);
                        }
                    });
            }
        }
    }

    @Test
	public void saveFileSuccesslly() throws IOException {
        String testContent = "HelloCamelQuarkusIntegrationTest";
		assumeThat(Files.exists(outputPath)).isFalse();

        RestAssured.given()
				.queryParam("content", testContent)
                .when()
				.post("/java/files")
                .then()
                .statusCode(200)
				.body(containsString("A new file was stored as"));


        try (Stream<Path> files = Files.list(outputPath)) {
            Path createdFile = files.findFirst()
                    .orElseThrow(() -> new AssertionError("No file was created in the output directory"));
			assertThat(createdFile).hasExtension("txt");
			assertThat(Files.readString(createdFile).trim()).isEqualTo(testContent);
        }
    }

    @Test
	public void failOnMissingParam() {
        RestAssured.given()
                .when()
				.post("/java/files")
                .then()
                .statusCode(400)
				.body(containsString("'content' query parameter is missing"));
    }
}
