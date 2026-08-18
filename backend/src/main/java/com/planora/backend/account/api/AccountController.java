package com.planora.backend.account.api;

import com.planora.backend.account.api.dto.AccountResponse;
import com.planora.backend.account.api.dto.CreateAccountRequest;
import com.planora.backend.account.api.dto.UpdateAccountRequest;
import com.planora.backend.account.application.create.CreateAccountCommand;
import com.planora.backend.account.application.create.CreateAccountUseCase;
import com.planora.backend.account.application.get.GetAccountByIdUseCase;
import com.planora.backend.account.application.get.GetAccountsUseCase;
import com.planora.backend.account.application.update.DeleteAccountUseCase;
import com.planora.backend.account.application.update.UpdateAccountCommand;
import com.planora.backend.account.application.update.UpdateAccountUseCase;
import com.planora.backend.account.domain.Account;
import com.planora.backend.common.api.PageResponse;
import com.planora.backend.common.pagination.PageRequest;
import com.planora.backend.common.pagination.PageResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final GetAccountByIdUseCase getAccountByIdUseCase;
    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountsUseCase getAccountsUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;

    public AccountController(GetAccountByIdUseCase getAccountByIdUseCase, CreateAccountUseCase createAccountUseCase, GetAccountsUseCase getAccountsUseCase, UpdateAccountUseCase updateAccountUseCase, DeleteAccountUseCase deleteAccountUseCase) {
        this.getAccountByIdUseCase = getAccountByIdUseCase;
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountsUseCase = getAccountsUseCase;
        this.updateAccountUseCase = updateAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
    }

    @GetMapping("/{accountId}")
    public AccountResponse getAccountById(
        @RequestParam UUID userId,
        @PathVariable UUID accountId
    ) {
        Account account = getAccountByIdUseCase.execute(userId, accountId);

        return toResponse(account);
    }

    @GetMapping
    public PageResponse<AccountResponse> getAccounts(
        @RequestParam UUID userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageRequest =
            new PageRequest(page, size);

        PageResult<Account> result =
            getAccountsUseCase.execute(
                userId,
                pageRequest
            );

        List<AccountResponse> content =
            result.content()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
            content,
            result.page(),
            result.size(),
            result.totalElements()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
        @RequestParam UUID userId,
        @Valid @RequestBody CreateAccountRequest request
    ) {
        CreateAccountCommand command = new CreateAccountCommand(
            userId,
            request.accountTypeId(),
            request.currencyId(),
            request.name()
        );

        Account account = createAccountUseCase.execute(userId, command);

        return toResponse(account);
    }

    @PutMapping("/{accountId}")
    public AccountResponse updateAccount(
        @RequestParam UUID userId,
        @PathVariable UUID accountId,
        @Valid @RequestBody UpdateAccountRequest request
    ) {
        UpdateAccountCommand command =
            new UpdateAccountCommand(
                userId,
                accountId,
                request.accountTypeId(),
                request.currencyId(),
                request.name()
            );

        Account account =
            updateAccountUseCase.execute(command);

        return toResponse(account);
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(
        @RequestParam UUID userId,
        @PathVariable UUID accountId
    ) {
        deleteAccountUseCase.execute(userId, accountId);
    }


    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getAccountTypeId(),
            account.getCurrencyId(),
            account.getName(),
            account.getBalance()
        );
    }
}
