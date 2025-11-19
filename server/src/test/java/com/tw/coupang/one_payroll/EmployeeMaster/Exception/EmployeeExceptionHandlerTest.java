package com.tw.coupang.one_payroll.EmployeeMaster.Exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeExceptionHandlerTest {

    private final EmployeeExceptionHandler handler = new EmployeeExceptionHandler();

    @Test
    void handleEmployeeConflictException_returns409() {
        EmployeeConflictException exception = new EmployeeConflictException("Employee ID already exists");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/employee");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<?> response = handler.handleEmployeeConflictException(exception, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<Object, Object> body = (Map<Object, Object>) response.getBody();
        assertEquals(409, body.get("status"));
        assertEquals("Conflict", body.get("error"));
        assertEquals("Employee ID already exists", body.get("message"));
    }

    @Test
    void handleIllegalArgumentException_returns400() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/employee");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<?> response = handler.handleIllegalArgumentException(exception, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<Object, Object> body = (Map<Object, Object>) response.getBody();
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals("Invalid input", body.get("message"));
    }

    @Test
    void handleGlobalException_returns500() {
        Exception exception = new Exception("Unexpected error");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/employee");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<?> response = handler.handleGlobalException(exception, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<Object, Object> body = (Map<Object, Object>) response.getBody();
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("Unexpected error", body.get("message"));
    }
}

