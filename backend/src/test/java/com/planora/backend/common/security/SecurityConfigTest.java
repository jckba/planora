package com.planora.backend.common.security;

import com.planora.backend.auth.api.AuthController;
import com.planora.backend.auth.application.user.CreateUserUseCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CreateUserUseCase createUserUseCase;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @ParameterizedTest
    @ValueSource(strings = {"/api/accounts", "/api/categories", "/api/expenses", "/api/purchases"})
    void shouldRejectAnonymousRequestsEvenWithUserIdParameter(String path) throws Exception {
        mockMvc.perform(get(path).param("userId", "00000000-0000-0000-0000-000000000001"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(createUserUseCase, userDetailsService);
    }
}
