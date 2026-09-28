package com.learning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionServiceTest {
    @Test
    void calculateBalanceCorrectly() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);

        transactionService.add(transactionSalary);
        transactionService.add(transactionFood);

        assertEquals(new BigDecimal("70.00"), transactionService.calculateBalance());
    }

    @Test
    void calculateBalanceEmptyCorrectly() {
        TransactionService transactionService = new TransactionService();

        assertEquals(BigDecimal.ZERO, transactionService.calculateBalance());
    }

    @Test
    void findAllReturnsUnmodifiableList() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);

        transactionService.add(transactionSalary);

        List<Transaction> transactionList = transactionService.findAll();

        assertThrows(
                UnsupportedOperationException.class,
                () -> transactionList.add((new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD)))
        );
        assertEquals(1,transactionService.findAll().size());
    }
}
