package com.hris.common.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void truncationDoesNotExposeTheInsertStatementToTheBrowser() {
        var response = new GlobalExceptionHandler().handleDuplicateKeyException(
                new DataIntegrityViolationException(
                        "could not execute statement [String or binary data would be truncated] "
                                + "[insert into annualization_snapshot (privateField) values (?)]"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("A value exceeds a database field's allowed length. Check the entered text and try again.",
                response.getBody());
    }
}
