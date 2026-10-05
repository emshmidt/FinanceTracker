package com.learning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {
    private static final LocalDate TRANSACTION_DATE = LocalDate.of(2025, 1, 15);

    @Test
    void shouldCreateValidTransaction() {
        Transaction transaction = new Transaction(
                "Зарплата", new BigDecimal("100.00"), TransactionType.INCOME,
                TRANSACTION_DATE, Category.SALARY
        );

        assertEquals("Зарплата", transaction.getTitle());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals(TRANSACTION_DATE, transaction.getDate());
        assertEquals(Category.SALARY, transaction.getCategory());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void shouldRejectNullOrBlankTitle(String title) {
        BigDecimal amount = new BigDecimal("10.00");

        assertThrows(
                InvalidTransactionException.class,
                () -> new Transaction(
                        title, amount, TransactionType.EXPENSE,
                        TRANSACTION_DATE, Category.FOOD
                )
        );
    }

    @Test
    void shouldRejectNullAmountWithInvalidTransactionException() {
        assertThrows(
                InvalidTransactionException.class,
                () -> new Transaction(
                        "Зарплата", null, TransactionType.INCOME,
                        TRANSACTION_DATE, Category.SALARY
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-10.00"})
    void shouldRejectNonPositiveAmount(String amountText) {
        BigDecimal amount = new BigDecimal(amountText);

        assertThrows(
                InvalidTransactionException.class,
                () -> new Transaction(
                        "Продукты", amount, TransactionType.EXPENSE,
                        TRANSACTION_DATE, Category.FOOD
                )
        );
    }

    @Test
    void shouldRejectAmountWithMoreThanTwoDecimalPlaces() {
        BigDecimal amount = new BigDecimal("10.256");

        assertThrows(
                InvalidTransactionException.class,
                () -> new Transaction(
                        "Зарплата", amount, TransactionType.INCOME,
                        TRANSACTION_DATE, Category.SALARY
                )
        );
    }

    @ParameterizedTest
    @CsvSource({
            "10, 10.00",
            "10.5, 10.50",
            "0.01, 0.01"
    })
    void shouldNormalizeAmountToTwoDecimalPlaces(String input, String expected) {
        BigDecimal amount = new BigDecimal(input);
        BigDecimal expectedAmount = new BigDecimal(expected);

        Transaction transaction = new Transaction(
                "Продукты", amount, TransactionType.EXPENSE,
                TRANSACTION_DATE, Category.FOOD
        );

        assertEquals(expectedAmount, transaction.getAmount());
    }

    @Test
    void shouldRejectNullType() {
        BigDecimal amount = new BigDecimal("10.00");

        assertThrows(
                InvalidTransactionException.class,
                () -> new Transaction(
                        "Зарплата", amount, null,
                        TRANSACTION_DATE, Category.SALARY
                )
        );
    }

    @Test
    void shouldRejectNullDate() {
        BigDecimal amount = new BigDecimal("10.00");

        assertThrows(
                InvalidTransactionException.class,
                () -> new Transaction(
                        "Зарплата", amount, TransactionType.INCOME,
                        null, Category.SALARY
                )
        );
    }

    @Test
    void shouldRejectNullCategory() {
        BigDecimal amount = new BigDecimal("10.00");

        assertThrows(
                InvalidTransactionException.class,
                () -> new Transaction(
                        "Зарплата", amount, TransactionType.INCOME,
                        TRANSACTION_DATE, null
                )
        );
    }
}
