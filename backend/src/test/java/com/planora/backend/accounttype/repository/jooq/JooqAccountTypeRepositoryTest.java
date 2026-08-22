package com.planora.backend.accounttype.repository.jooq;

import com.planora.backend.accounttype.repository.AccountTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JooqAccountTypeRepositoryTest {

    @Autowired
    private AccountTypeRepository accountTypeRepository;

    @Test
    void shouldReturnTrueWhenAccountTypeExists() {
        assertTrue(
            accountTypeRepository.existsById((short) 1)
        );
    }

    @Test
    void shouldReturnFalseWhenAccountTypeDoesNotExist() {
        assertFalse(
            accountTypeRepository.existsById((short) 999)
        );
    }

}
