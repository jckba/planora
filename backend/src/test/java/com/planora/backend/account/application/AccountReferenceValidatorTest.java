package com.planora.backend.account.application;

import com.planora.backend.account.repository.AccountRepository;
import com.planora.backend.accounttype.repository.AccountTypeRepository;
import com.planora.backend.common.exception.InvalidAccountReferenceException;
import com.planora.backend.currency.repository.CurrencyRepository;
import com.planora.persistence.jooq.tables.AccountType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountReferenceValidatorTest {

    @Mock
    private AccountTypeRepository accountTypeRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private AccountReferenceValidator validator;

    @Test
    void shouldAcceptValidReferences() {
        when(
            accountTypeRepository.existsById((short) 1)
        ).thenReturn(true);

        when(
            currencyRepository.existsById((short) 1)
        ).thenReturn(true);

        assertDoesNotThrow(
            () -> validator.validate(
                (short) 1,
                (short) 1
            )
        );
    }

    @Test
    void shouldRejectUnknownAccountType() {
        when(
            accountTypeRepository.existsById((short) 99)
        ).thenReturn(false);

        assertThrows(
            InvalidAccountReferenceException.class,
            () -> validator.validate(
                (short) 99,
                (short) 1
            )
        );

        verify(currencyRepository, never())
            .existsById(anyShort());
    }

    @Test
    void shouldRejectUnknownCurrency() {
        when(
            accountTypeRepository.existsById((short) 1)
        ).thenReturn(true);

        when(
            currencyRepository.existsById((short) 99)
        ).thenReturn(false);

        assertThrows(
            InvalidAccountReferenceException.class,
            () -> validator.validate(
                (short) 1,
                (short) 99
            )
        );
    }

}
