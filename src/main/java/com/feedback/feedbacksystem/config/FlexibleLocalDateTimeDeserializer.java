package com.feedback.feedbacksystem.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Reads a {@link LocalDateTime} from either a plain date ("2026-08-01") or a full
 * ISO date-time ("2026-08-01T09:30:00").
 *
 * <p>The API contract documents the short form, while the entities store a timestamp.
 * A plain date is taken as the start of that day.
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final int PLAIN_DATE_LENGTH = "yyyy-MM-dd".length();

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String raw = parser.getText();
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim();
        try {
            return value.length() == PLAIN_DATE_LENGTH
                    ? LocalDate.parse(value).atStartOfDay()
                    : LocalDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            // Surfaces as HttpMessageNotReadableException, which the advice maps to 400.
            throw context.weirdStringException(value, LocalDateTime.class,
                    "Expected a date (yyyy-MM-dd) or a date-time (yyyy-MM-ddTHH:mm:ss)");
        }
    }
}
