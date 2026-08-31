package com.planora.backend.common.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@Import({
    GlobalExceptionHandler.class,
    GlobalExceptionHandlerTest.TestController.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("Resource not found");
        }

        @GetMapping("/test/invalid-request")
        public void invalidRequest() {
            throw new IllegalArgumentException("Invalid request");
        }

        @GetMapping("/test/optimistic-lock")
        public void optimisticLock() {
            throw new OptimisticLockException(
                "Resource was modified by another transaction"
            );
        }

        @GetMapping("/test/invalid-account")
        public void invalidAccount() {
            throw new InvalidAccountReferenceException(
                "Account not found"
            );
        }

        @GetMapping("/test/invalid-expense")
        public void invalidExpense() {
            throw new InvalidExpenseReferenceException(
                "Expense not found"
            );
        }

        @PostMapping("/test/validation")
        public void validation(
            @Valid @RequestBody TestRequest request
        ) {
        }

        record TestRequest(
            @NotBlank
            String name
        ) {
        }

    }

    @Test
    void shouldHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(
                get("/test/not-found")
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("RESOURCE_NOT_FOUND")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Resource not found")
            )
            .andExpect(
                jsonPath("$.errors.length()")
                    .value(0)
            );
    }
    @Test
    void shouldHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(
                get("/test/invalid-request")
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value("INVALID_ARGUMENT")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Invalid request")
            )
            .andExpect(
                jsonPath("$.errors.length()")
                    .value(0)
            );
    }
    @Test
    void shouldHandleOptimisticLockException() throws Exception {
        mockMvc.perform(
                get("/test/optimistic-lock")
            )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.code")
                    .value("OPTIMISTIC_LOCK")
            )
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Resource was modified by another transaction"
                    )
            );
    }
    @Test
    void shouldHandleInvalidAccountReferenceException()
        throws Exception {

        mockMvc.perform(
                get("/test/invalid-account")
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value("INVALID_ACCOUNT_REFERENCE")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Account not found")
            );
    }
    @Test
    void shouldHandleInvalidExpenseReferenceException()
        throws Exception {

        mockMvc.perform(
                get("/test/invalid-expense")
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("INVALID_EXPENSE_REFERENCE")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Expense not found")
            );
    }
    @Test
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(
                post("/test/validation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "name": ""
                }
                """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value("VALIDATION_ERROR")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Request validation failed")
            )
            .andExpect(
                jsonPath("$.errors.length()")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.errors[0].field")
                    .value("name")
            );
    }
}
