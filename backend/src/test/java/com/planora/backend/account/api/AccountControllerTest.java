package com.planora.backend.account.api;

import com.planora.backend.account.application.create.CreateAccountCommand;
import com.planora.backend.account.application.create.CreateAccountUseCase;
import com.planora.backend.account.application.get.GetAccountByIdUseCase;
import com.planora.backend.account.application.get.GetAccountsUseCase;
import com.planora.backend.account.application.delete.DeleteAccountUseCase;
import com.planora.backend.account.application.update.UpdateAccountCommand;
import com.planora.backend.account.application.update.UpdateAccountUseCase;
import com.planora.backend.account.domain.Account;
import com.planora.backend.common.api.GlobalExceptionHandler;
import com.planora.backend.common.exception.InvalidAccountReferenceException;
import com.planora.backend.common.exception.OptimisticLockException;
import com.planora.backend.common.exception.ResourceNotFoundException;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAccountByIdUseCase getAccountByIdUseCase;

    @MockitoBean
    private CreateAccountUseCase createAccountUseCase;

    @MockitoBean
    private GetAccountsUseCase getAccountsUseCase;

    @MockitoBean
    private UpdateAccountUseCase updateAccountUseCase;

    @MockitoBean
    private DeleteAccountUseCase deleteAccountUseCase;

    @Test
    void shouldGetAccountById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "My Account"
        );

        when(
            getAccountByIdUseCase.execute(
                userId,
                accountId
            )
        ).thenReturn(account);

        mockMvc.perform(
                get(
                    "/api/accounts/{accountId}",
                    accountId
                )
                    .param("userId", userId.toString())
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(account.getId().toString())
            )
            .andExpect(
                jsonPath("$.accountTypeId")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.currencyId")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.name")
                    .value("My Account")
            )
            .andExpect(
                jsonPath("$.balance")
                    .value(0)
            );

        verify(getAccountByIdUseCase)
            .execute(userId, accountId);
    }

    @Test
    void shouldReturn404WhenAccountDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(
            getAccountByIdUseCase.execute(
                userId,
                accountId
            )
        ).thenThrow(
            new ResourceNotFoundException("Account not found")
        );

        mockMvc.perform(
                get(
                    "/api/accounts/{accountId}",
                    accountId
                )
                    .param("userId", userId.toString())
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("RESOURCE_NOT_FOUND")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Account not found")
            );

        verify(getAccountByIdUseCase)
            .execute(userId, accountId);
    }

    @Test
    void shouldCreateAccount() throws Exception {
        UUID userId = UUID.randomUUID();

        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "My Account"
        );

        CreateAccountCommand command =
            new CreateAccountCommand(
                userId,
                (short) 1,
                (short) 1,
                "My Account"
            );

        when(
            createAccountUseCase.execute(userId, command)
        ).thenReturn(account);

        mockMvc.perform(
                post("/api/accounts")
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "accountTypeId": 1,
                    "currencyId": 1,
                    "name": "My Account"
                }
                """)
            )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.id")
                    .value(account.getId().toString())
            )
            .andExpect(
                jsonPath("$.accountTypeId")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.currencyId")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.name")
                    .value("My Account")
            )
            .andExpect(
                jsonPath("$.balance")
                    .value(0)
            );

        verify(createAccountUseCase)
            .execute(userId, command);
    }

    @Test
    void shouldRejectCreateAccountWithoutBody() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(
                post("/api/accounts")
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());

        verifyNoInteractions(createAccountUseCase);
    }

    @Test
    void shouldGetAccounts() throws Exception {
        UUID userId = UUID.randomUUID();

        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "My Account"
        );

        PageRequest pageRequest =
            new PageRequest(0, 10);

        PageResult<Account> pageResult =
            new PageResult<>(
                List.of(account),
                0,
                10,
                1
            );

        when(
            getAccountsUseCase.execute(
                userId,
                pageRequest
            )
        ).thenReturn(pageResult);

        mockMvc.perform(
                get("/api/accounts")
                    .param("userId", userId.toString())
                    .param("page", "0")
                    .param("size", "10")
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content.length()")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.content[0].id")
                    .value(account.getId().toString())
            )
            .andExpect(
                jsonPath("$.content[0].name")
                    .value("My Account")
            )
            .andExpect(
                jsonPath("$.content[0].balance")
                    .value(0)
            )
            .andExpect(
                jsonPath("$.page")
                    .value(0)
            )
            .andExpect(
                jsonPath("$.size")
                    .value(10)
            )
            .andExpect(
                jsonPath("$.totalElements")
                    .value(1)
            );

        verify(getAccountsUseCase)
            .execute(userId, pageRequest);
    }

    @Test
    void shouldUpdateAccount() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        Account account = Account.create(
            userId,
            (short) 1,
            (short) 1,
            "Updated Account"
        );

        UpdateAccountCommand command =
            new UpdateAccountCommand(
                userId,
                accountId,
                (short) 1,
                (short) 1,
                "Updated Account"
            );

        when(
            updateAccountUseCase.execute(command)
        ).thenReturn(account);

        mockMvc.perform(
                put("/api/accounts/{accountId}", accountId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "accountTypeId": 1,
                    "currencyId": 1,
                    "name": "Updated Account"
                }
                """)
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id")
                    .value(account.getId().toString())
            )
            .andExpect(
                jsonPath("$.name")
                    .value("Updated Account")
            )
            .andExpect(
                jsonPath("$.balance")
                    .value(0)
            );

        verify(updateAccountUseCase)
            .execute(command);
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingAccount()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        UpdateAccountCommand command =
            new UpdateAccountCommand(
                userId,
                accountId,
                (short) 1,
                (short) 1,
                "Updated Account"
            );

        when(
            updateAccountUseCase.execute(command)
        ).thenThrow(
            new ResourceNotFoundException("Account not found")
        );

        mockMvc.perform(
                put("/api/accounts/{accountId}", accountId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "accountTypeId": 1,
                    "currencyId": 1,
                    "name": "Updated Account"
                }
                """)
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("RESOURCE_NOT_FOUND")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Account not found")
            );

        verify(updateAccountUseCase)
            .execute(command);
    }

    @Test
    void shouldReturn409WhenAccountWasModifiedConcurrently()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        UpdateAccountCommand command =
            new UpdateAccountCommand(
                userId,
                accountId,
                (short) 1,
                (short) 1,
                "Updated Account"
            );

        when(
            updateAccountUseCase.execute(command)
        ).thenThrow(
            new OptimisticLockException(
                "Account was modified by another transaction"
            )
        );

        mockMvc.perform(
                put("/api/accounts/{accountId}", accountId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "accountTypeId": 1,
                    "currencyId": 1,
                    "name": "Updated Account"
                }
                """)
            )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.code")
                    .value("OPTIMISTIC_LOCK")
            )
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Account was modified by another transaction"
                    )
            );

        verify(updateAccountUseCase)
            .execute(command);
    }

    @Test
    void shouldDeleteAccount() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        doNothing().when(deleteAccountUseCase)
            .execute(userId, accountId);

        mockMvc.perform(
                delete("/api/accounts/{accountId}", accountId)
                    .param("userId", userId.toString())
            )
            .andExpect(status().isNoContent());

        verify(deleteAccountUseCase)
            .execute(userId, accountId);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingAccount()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        doThrow(
            new ResourceNotFoundException("Account not found")
        )
            .when(deleteAccountUseCase)
            .execute(userId, accountId);

        mockMvc.perform(
                delete("/api/accounts/{accountId}", accountId)
                    .param("userId", userId.toString())
            )
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.code")
                    .value("RESOURCE_NOT_FOUND")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Account not found")
            );

        verify(deleteAccountUseCase)
            .execute(userId, accountId);
    }

    @Test
    void shouldReturn400WhenAccountTypeDoesNotExist()
        throws Exception {

        UUID userId = UUID.randomUUID();

        when(
            createAccountUseCase.execute(
                eq(userId),
                any(CreateAccountCommand.class)
            )
        ).thenThrow(
            new InvalidAccountReferenceException(
                "Account type not found"
            )
        );

        mockMvc.perform(
                post("/api/accounts")
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "accountTypeId": 99,
                    "currencyId": 1,
                    "name": "My Account"
                }
                """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value("INVALID_ACCOUNT_REFERENCE")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Account type not found")
            );
    }

    @Test
    void shouldReturn400WhenCurrencyDoesNotExist()
        throws Exception {

        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        UpdateAccountCommand command =
            new UpdateAccountCommand(
                userId,
                accountId,
                (short) 1,
                (short) 99,
                "Updated Account"
            );

        when(
            updateAccountUseCase.execute(command)
        ).thenThrow(
            new InvalidAccountReferenceException(
                "Currency not found"
            )
        );

        mockMvc.perform(
                put("/api/accounts/{accountId}", accountId)
                    .param("userId", userId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                {
                    "accountTypeId": 1,
                    "currencyId": 99,
                    "name": "Updated Account"
                }
                """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.code")
                    .value("INVALID_ACCOUNT_REFERENCE")
            )
            .andExpect(
                jsonPath("$.message")
                    .value("Currency not found")
            );

        verify(updateAccountUseCase)
            .execute(command);
    }


}
