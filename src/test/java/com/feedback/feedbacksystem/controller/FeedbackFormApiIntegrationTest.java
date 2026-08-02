package com.feedback.feedbacksystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Drives the form endpoints over real HTTP against the running application, on the same
 * in-memory profile used by {@code ./mvnw spring-boot:run -Dspring-boot.run.profiles=h2}.
 *
 * <p>Unlike the {@code @WebMvcTest} suite this boots the whole context, so it is what proves
 * the security chain lets the API through and that the service and repository layers behave
 * against a database rather than a mock.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
class FeedbackFormApiIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("the survey builder form endpoints work end to end over HTTP")
    void formEndpointsWorkEndToEnd() throws Exception {
        // Reachable at all: the security chain must not challenge an API call.
        ResponseEntity<String> active = rest.getForEntity("/api/forms/active", String.class);
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
        ResponseEntity<String> fetched = rest.getForEntity("/api/forms/" + formId, String.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode fetchedBody = objectMapper.readTree(fetched.getBody());
        assertThat(fetchedBody.get("title").asText()).isEqualTo("Faculty Feedback - CSE");
        assertThat(fetchedBody.get("totalQuestions").asLong()).isZero();

        // Publishing is refused while the form has no questions.
        ResponseEntity<String> published = rest.exchange("/api/forms/" + formId + "/publish",
                HttpMethod.PUT, HttpEntity.EMPTY, String.class);
        assertThat(published.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(objectMapper.readTree(published.getBody()).get("message").asText())
                .contains("at least one question");

        // Unknown form.
        ResponseEntity<String> missing = rest.getForEntity("/api/forms/999999", String.class);
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

        // The creator header is required until authentication supplies it.
        ResponseEntity<String> noHeader = rest.exchange("/api/forms", HttpMethod.POST,
                json("""
                        {"title":"Headerless","startDate":"2026-08-01","endDate":"2026-08-10"}
                        """, null), String.class);
        assertThat(noHeader.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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
                         "options":["Excellent","Good","Average","Poor"],"displayOrder":2,"required":true}
                        """.formatted(formId), null), String.class);
        assertThat(radio.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Reusing a display order collides.
        ResponseEntity<String> duplicateOrder = rest.exchange("/api/questions", HttpMethod.POST,
                json("""
                        {"formId":%d,"questionText":"Anything else?","questionType":"TEXT","displayOrder":2}
                        """.formatted(formId), null), String.class);
        assertThat(duplicateOrder.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Questions come back in display order, using the contract field names.
        JsonNode questions = objectMapper.readTree(
                rest.getForEntity("/api/questions/form/" + formId, String.class).getBody());
        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).get("questionText").asText()).isEqualTo("How do you rate the faculty?");
        assertThat(questions.get(0).get("required").asBoolean()).isTrue();
        assertThat(questions.get(0).has("mandatory")).isFalse();
        assertThat(questions.get(1).get("options")).hasSize(4);

        // Publish, after which the form is closed for editing.
        ResponseEntity<String> published = rest.exchange("/api/forms/" + formId + "/publish",
                HttpMethod.PUT, HttpEntity.EMPTY, String.class);
        assertThat(published.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> lateQuestion = rest.exchange("/api/questions", HttpMethod.POST,
                json("""
                        {"formId":%d,"questionText":"Too late","questionType":"TEXT"}
                        """.formatted(formId), null), String.class);
        assertThat(lateQuestion.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Target the published form at a department wide audience.
        String assignment = """
                {"formId":%d,"departmentId":1,"semester":5,"section":"A",
                 "batch":"2023-2027","academicYear":"2026-2027"}
                """.formatted(formId);

        ResponseEntity<String> assigned = rest.exchange("/api/assignments", HttpMethod.POST,
                json(assignment, null), String.class);
        assertThat(assigned.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(objectMapper.readTree(assigned.getBody()).get("message").asText())
                .isEqualTo("Feedback form assigned successfully.");

        // The same target twice is a conflict.
        ResponseEntity<String> assignedAgain = rest.exchange("/api/assignments", HttpMethod.POST,
                json(assignment, null), String.class);
        assertThat(assignedAgain.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // The entities also validate formats. A malformed academic year must read as a bad
        // request rather than escaping as a 500.
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
                rest.getForEntity("/api/forms/active", String.class).getBody());
        assertThat(active).anySatisfy(form -> assertThat(form.get("id").asLong()).isEqualTo(formId));
    }

    private HttpEntity<String> json(String body, Long creatorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (creatorId != null) {
            headers.add("X-User-Id", String.valueOf(creatorId));
        }
        return new HttpEntity<>(body, headers);
    }
}
