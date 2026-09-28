package com.learning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class TransactionTest {

    @Test
    void createsValidTransaction() {
        Transaction transaction = new Transaction("Sal", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        assertEquals("Sal", transaction.getTitle());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals(LocalDate.of(2025, 1, 15), transaction.getDate());
        assertEquals(Category.SALARY, transaction.getCategory());
    }

    @Test
    void rejectsNullTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(null, new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY)
        );
    }

    @Test
    void rejectsBlankTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction("     ", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY)
        );
    }

    @Test
    void rejectsEmptyTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction("", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY)
        );
    }
    @Test
    void rejectsNullAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction("A", null, TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY)
        );
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction("A", BigDecimal.ZERO, TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY)
        );
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction("A", new BigDecimal("-10.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY)
        );
    }

    @Test
    void rejectsNullType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction("A", new BigDecimal("10.00"), null, LocalDate.of(2025, 1, 15), Category.SALARY)
        );
    }

    @Test
    void rejectsNullDate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction("A", new BigDecimal("10.00"), TransactionType.INCOME, null, Category.SALARY)
        );
    }

    @Test
    void rejectsNullCategory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction("A", new BigDecimal("10.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), null)
        );
    }
}
