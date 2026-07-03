package com.github.aha.poc.quarkus;

import static com.github.aha.poc.quarkus.FileApiRoute.HEADER_CONTENT;
import static io.restassured.RestAssured.given;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@Slf4j
public class XmlConfigRouteTest {

	final static String ROOT_PATH = "/xml/files";
	final static String TEST_CONTENT = "HelloCamelQuarkusIntegrationTest";

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

    @Nested
    class SaveFile {
    	
    	@Test
    	public void defaulName() throws IOException {
    		assumeThat(Files.exists(outputPath)).isFalse();
    		
    		given()
			    .queryParam(HEADER_CONTENT, TEST_CONTENT)
		    .when()
			    .post(ROOT_PATH)
		    .then()
	            .statusCode(200)
				.body(containsString("A new file was stored as"));
    		
    		
    		try (Stream<Path> files = Files.list(outputPath)) {
    			Path createdFile = files.findFirst()
    					.orElseThrow(() -> new AssertionError("No file was created in the output directory"));
    			assertThat(createdFile).hasExtension("txt");
				assertThat(Files.readString(createdFile).trim()).isEqualTo(TEST_CONTENT);
    		}
    	}
    	
		@Test
		public void definedName() throws IOException {
			assumeThat(Files.exists(outputPath)).isFalse();
			var testFileName = "custom-file-name.txt";

			given()
			    .queryParam(HEADER_CONTENT, TEST_CONTENT)
			    .queryParam("fileName", testFileName)
			.when()
			    .post(ROOT_PATH)
			.then()
			    .statusCode(200)
			    .body(containsString("A new file was stored as"));

			try (Stream<Path> files = Files.list(outputPath)) {
				Path createdFile = files.findFirst()
				    .orElseThrow(() -> new AssertionError("No file was created in the output directory"));
				assertThat(createdFile.getFileName().toString()).isEqualTo(testFileName);
				assertThat(Files.readString(createdFile).trim()).isEqualTo(TEST_CONTENT);
			}
		}

    }

    @Test
	public void failOnMissingParam() {
		given()
        .when()
		    .post(ROOT_PATH)
        .then()
        	.statusCode(400)
        	.body(containsString("'content' query parameter is missing"));
    }
}
