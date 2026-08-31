package com.planora.backend.common.pagination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageRequestTest {

    @Test
    void shouldCreateValidPageRequest() {
        PageRequest pageRequest = new PageRequest(0, 10);
        assertEquals(0, pageRequest.page());
        assertEquals(10, pageRequest.size());
    }

    @Test
    void shouldAllowPageZero() {
        assertDoesNotThrow(
            () -> new PageRequest(0, 10)
        );
    }

    @Test
    void shouldRejectNegativePage() {
        IllegalArgumentException exception =assertThrows(
            IllegalArgumentException.class,
            () -> new PageRequest(-1, 10)
        );
        assertEquals("Page number cannot be negative",
            exception.getMessage()
        );
    }

    @Test
    void shouldRejectZeroSize() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PageRequest(0, 0)
        );
        assertEquals("Page size must be greater than 0",
            exception.getMessage()
        );
    }

    @Test
    void shouldRejectNegativeSize() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PageRequest(0, -1)
        );
        assertEquals("Page size must be greater than 0",
            exception.getMessage()
        );
    }

    @Test
    void shouldAllowMaximumPageSize() {
        assertDoesNotThrow(
            () -> new PageRequest(0, 100)
        );
    }

    @Test
    void shouldRejectPageSizeGreaterThanMaximum() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PageRequest(0, 101)
        );
        assertEquals("Page size cannot be greater than 100",
            exception.getMessage()
        );
    }
}
