package de.sfl;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationStartIT {

	private static final String BASE_URL = "http://" + System.getProperty("docker.hostName", "localhost") + ":8080";
	private static final String HEALTH_API = BASE_URL + "/actuator/health";

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Test
	void healthCheckReturnsOk() throws Exception {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(HEALTH_API))
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("UP"));
	}
}
