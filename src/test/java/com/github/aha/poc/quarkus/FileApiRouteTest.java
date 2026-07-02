package com.github.aha.poc.quarkus;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

@QuarkusTest
public class FileApiRouteTest {

    // We inject the configured directory to know where to look for files during the test
    @ConfigProperty(name = "app.output.dir")
    String outputDir;

    private Path outputPath;

    @BeforeEach
    public void setup() {
        outputPath = Paths.get(outputDir);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        // Clean up generated files after each test so they don't pile up
        if (Files.exists(outputPath)) {
            try (Stream<Path> walk = Files.walk(outputPath)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Log or ignore delete failures during cleanup
                        }
                    });
            }
        }
    }

    @Test
    public void testSaveFileSuccess() throws IOException {
        String testContent = "HelloCamelQuarkusIntegrationTest";

        // 1. Send POST request with the 'cnt' query parameter
        RestAssured.given()
				.queryParam("ctnt", testContent)
                .when()
                .post("/files/content")
                .then()
                .statusCode(200)
				.body(containsString("A new file was stored as"));

        // 2. Verify that the file was actually created on disk
        assertTrue(Files.exists(outputPath), "The output directory should exist");

        // Find the created file (since the name contains a dynamic timestamp, we list the directory)
        try (Stream<Path> files = Files.list(outputPath)) {
            Path createdFile = files.findFirst()
                    .orElseThrow(() -> new AssertionError("No file was created in the output directory"));

            // 3. Verify the file extension and its contents
            assertTrue(createdFile.getFileName().toString().endsWith(".txt"), "File should be a .txt file");
            
            String fileContent = Files.readString(createdFile).trim();
            assertEquals(testContent, fileContent, "The file content should match the query parameter value");
        }
    }

    @Test
    public void testSaveFileMissingParam() {
        // Test behavior when the required 'cnt' parameter is omitted
        RestAssured.given()
                .when()
                .post("/files/content")
                .then()
                .statusCode(400)
				.body(containsString("'ctnt' query parameter is missing"));
    }
}
