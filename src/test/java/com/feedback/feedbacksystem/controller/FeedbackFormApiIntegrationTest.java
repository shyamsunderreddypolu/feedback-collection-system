package com.feedback.feedbacksystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.feedbacksystem.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Drives the form endpoints over real HTTP against the running application.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
class FeedbackFormApiIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("the survey builder form endpoints work end to end over HTTP")
    void formEndpointsWorkEndToEnd() throws Exception {
        // Reachable at all: the security chain accepts valid bearer token.
        ResponseEntity<String> active = rest.exchange("/api/forms/active", HttpMethod.GET, authHeader(), String.class);
        assertThat(active.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Create, using the short date form from the API contract.
        ResponseEntity<String> created = rest.exchange("/api/forms", HttpMethod.POST,
                json("""
                        {
                          "title": "Faculty Feedback - CSE",
                          "description": "Semester End Faculty Feedback",
                          "startDate": "2026-08-01",
                          "endDate": "2026-08-10"
                        }
                        """, 1L), String.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode createdBody = objectMapper.readTree(created.getBody());
        assertThat(createdBody.get("status").asText()).isEqualTo("DRAFT");
        assertThat(createdBody.get("category").asText()).isEqualTo("GENERAL");
        assertThat(createdBody.get("creatorName").asText()).isEqualTo("Demo Admin");
        assertThat(createdBody.get("startDate").asText()).startsWith("2026-08-01T00:00");
        long formId = createdBody.get("id").asLong();

        // Read back.
        ResponseEntity<String> fetched = rest.exchange("/api/forms/" + formId, HttpMethod.GET, authHeader(), String.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode fetchedBody = objectMapper.readTree(fetched.getBody());
        assertThat(fetchedBody.get("title").asText()).isEqualTo("Faculty Feedback - CSE");
        assertThat(fetchedBody.get("totalQuestions").asLong()).isZero();

        // Publishing is refused while the form has no questions.
        ResponseEntity<String> published = rest.exchange("/api/forms/" + formId + "/publish",
                HttpMethod.PUT, authHeader(), String.class);
        assertThat(published.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(objectMapper.readTree(published.getBody()).get("message").asText())
                .contains("at least one question");

        // Unknown form.
        ResponseEntity<String> missing = rest.exchange("/api/forms/999999", HttpMethod.GET, authHeader(), String.class);
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // Validation failure carries the offending field.
        ResponseEntity<String> invalid = rest.exchange("/api/forms", HttpMethod.POST,
                json("""
                        {"title":"  ","startDate":"2026-08-01","endDate":"2026-08-10"}
                        """, 1L), String.class);
        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(objectMapper.readTree(invalid.getBody()).get("fieldErrors").has("title")).isTrue();

        // Unknown creator is a 404 rather than a 500.
        ResponseEntity<String> unknownCreator = rest.exchange("/api/forms", HttpMethod.POST,
                json("""
                        {"title":"Orphan Form","startDate":"2026-08-01","endDate":"2026-08-10"}
                        """, 4242L), String.class);
        assertThat(unknownCreator.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("a form can be built, published and targeted over the API in one pass")
    void surveyBuilderFlowEndToEnd() throws Exception {
        // Anchored on today so the form is inside its response window whenever this runs.
        String startDate = LocalDate.now().minusDays(1).toString();
        String endDate = LocalDate.now().plusDays(30).toString();

        long formId = objectMapper.readTree(rest.exchange("/api/forms", HttpMethod.POST,
                        json("""
                                {"title":"Course Feedback","startDate":"%s","endDate":"%s"}
                                """.formatted(startDate, endDate), 1L), String.class)
                .getBody()).get("id").asLong();

        // A rating question needs no options.
        ResponseEntity<String> rating = rest.exchange("/api/questions", HttpMethod.POST,
                json("""
                        {"formId":%d,"questionText":"How do you rate the faculty?",
                         "questionType":"RATING","displayOrder":1,"required":true}
                        """.formatted(formId), null), String.class);
        assertThat(rating.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // A choice question offering a single answer is not a choice.
        ResponseEntity<String> tooFewOptions = rest.exchange("/api/questions", HttpMethod.POST,
                json("""
                        {"formId":%d,"questionText":"Teaching Method","questionType":"RADIO",
                         "options":["Good"],"displayOrder":2,"required":true}
                        """.formatted(formId), null), String.class);
        assertThat(tooFewOptions.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(objectMapper.readTree(tooFewOptions.getBody()).get("message").asText())
                .contains("at least 2 options");

        ResponseEntity<String> radio = rest.exchange("/api/questions", HttpMethod.POST,
                json("""
                        {"formId":%d,"questionText":"Teaching Method","questionType":"RADIO",
                         "options":["Interactive","Slide Based","Mixed"],"displayOrder":2,"required":true}
                        """.formatted(formId), null), String.class);
        assertThat(radio.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Form builder views the questions in display order.
        JsonNode questions = objectMapper.readTree(
                rest.exchange("/api/questions/form/" + formId, HttpMethod.GET, authHeader(), String.class).getBody());
        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).get("questionType").asText()).isEqualTo("RATING");
        assertThat(questions.get(1).get("options")).hasSize(3);

        // Once questions are present the form publishes successfully.
        ResponseEntity<String> publish = rest.exchange("/api/forms/" + formId + "/publish",
                HttpMethod.PUT, authHeader(), String.class);
        assertThat(publish.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Target audience: department 1, course 1, semester 6, section B, batch 2023-2027.
        ResponseEntity<String> target = rest.exchange("/api/assignments", HttpMethod.POST,
                json("""
                        {"formId":%d,"departmentId":1,"courseId":1,"semester":6,"section":"B",
                         "batch":"2023-2027","academicYear":"2026-2027"}
                        """.formatted(formId), null), String.class);
        assertThat(target.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Re-assigning the same target is rejected.
        ResponseEntity<String> duplicateTarget = rest.exchange("/api/assignments", HttpMethod.POST,
                json("""
                        {"formId":%d,"departmentId":1,"courseId":1,"semester":6,"section":"B",
                         "batch":"2023-2027","academicYear":"2026-2027"}
                        """.formatted(formId), null), String.class);
        assertThat(duplicateTarget.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Validation failure on target attributes is returned as a bad request.
        ResponseEntity<String> malformedYear = rest.exchange("/api/assignments", HttpMethod.POST,
                json("""
                        {"formId":%d,"departmentId":1,"semester":6,"section":"B",
                         "batch":"2023-2027","academicYear":"2026-27"}
                        """.formatted(formId), null), String.class);
        assertThat(malformedYear.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(objectMapper.readTree(malformedYear.getBody()).get("fieldErrors").has("academicYear"))
                .isTrue();

        // A published form inside its window shows up as active.
        JsonNode active = objectMapper.readTree(
                rest.exchange("/api/forms/active", HttpMethod.GET, authHeader(), String.class).getBody());
        assertThat(active).anySatisfy(form -> assertThat(form.get("id").asLong()).isEqualTo(formId));
    }

    private HttpEntity<String> json(String body, Long creatorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String token = jwtTokenProvider.generateTokenForUser("admin@college.edu", creatorId != null ? creatorId : 1L, "ROLE_ADMIN");
        headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> authHeader() {
        HttpHeaders headers = new HttpHeaders();
        String token = jwtTokenProvider.generateTokenForUser("admin@college.edu", 1L, "ROLE_ADMIN");
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }
}
