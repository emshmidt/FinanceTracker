package com.learning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InMemoryTransactionRepositoryTest {
    private InMemoryTransactionRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryTransactionRepository();
    }

    @Test
    void shouldSaveTransaction() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);

        repository.save(transaction);

        assertEquals(List.of(transaction), repository.findAll());
    }

    @Test
    void shouldRejectNullTransactionWithoutChangingState() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);

        repository.save(transaction);

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );
        assertEquals(List.of(transaction), repository.findAll());
    }

    @Test
    void shouldRejectDuplicateIdWithoutChangingState() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);
        Transaction transaction2 = new Transaction(
                "Salary", new BigDecimal("300.00"),
                TransactionType.INCOME,
                LocalDate.of(2025, 1, 1),
                Category.SALARY);

        repository.save(transaction);
        repository.save(transaction2);

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(transaction)
        );
        assertEquals(List.of(transaction, transaction2), repository.findAll());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void shouldFindTransactionById() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);

        repository.save(transaction);

        assertEquals(Optional.of(transaction), repository.findById(transaction.getId()));
    }

    @Test
    void shouldReturnEmptyOptionalWhenRepositoryIsEmpty() {
        assertEquals(Optional.empty(), repository.findById(UUID.randomUUID()));
    }

    @Test
    void shouldReturnEmptyOptionalForUnknownId() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);

        repository.save(transaction);

        assertEquals(Optional.empty(), repository.findById(UUID.randomUUID()));
    }

    @Test
    void shouldRejectNullIdWhenFindingTransaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findById(null)
        );
    }

    @Test
    void shouldReturnEmptyListWhenRepositoryIsEmpty() {
        assertEquals(List.of(), repository.findAll());
    }

    @Test
    void shouldReturnTransactionsInInsertionOrder() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);
        Transaction transaction2 = new Transaction(
                "Salary", new BigDecimal("300.00"),
                TransactionType.INCOME,
                LocalDate.of(2025, 1, 1),
                Category.SALARY);
        Transaction transaction3 = new Transaction(
                "Other", new BigDecimal("50.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.OTHER);

        repository.save(transaction);
        repository.save(transaction2);
        repository.save(transaction3);

        assertEquals(List.of(transaction, transaction2, transaction3), repository.findAll());
    }

    @Test
    void shouldReturnUnmodifiableList() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);

        repository.save(transaction);

        assertThrows(
                UnsupportedOperationException.class,
                () -> repository.findAll().clear()
        );
    }

    @Test
    void shouldPreserveSnapshotWhenTransactionIsAddedLater() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);
        Transaction transaction2 = new Transaction(
                "Salary", new BigDecimal("300.00"),
                TransactionType.INCOME,
                LocalDate.of(2025, 1, 1),
                Category.SALARY);

        repository.save(transaction);
        List<Transaction> found = repository.findAll();
        repository.save(transaction2);

        assertEquals(List.of(transaction), found);
        assertEquals(List.of(transaction, transaction2), repository.findAll());
    }

    @Test
    void shouldPreserveSnapshotWhenTransactionIsDeletedLater() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);
        Transaction transaction2 = new Transaction(
                "Salary", new BigDecimal("300.00"),
                TransactionType.INCOME,
                LocalDate.of(2025, 1, 1),
                Category.SALARY);

        repository.save(transaction);
        repository.save(transaction2);
        List<Transaction> found = repository.findAll();
        repository.deleteById(transaction2.getId());

        assertEquals(List.of(transaction), repository.findAll());
        assertEquals(List.of(transaction, transaction2), found);
    }

    @Test
    void shouldDeleteOnlyTransactionWithMatchingId() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);
        Transaction transaction2 = new Transaction(
                "Salary", new BigDecimal("300.00"),
                TransactionType.INCOME,
                LocalDate.of(2025, 1, 1),
                Category.SALARY);
        Transaction transaction3 = new Transaction(
                "Other", new BigDecimal("50.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.OTHER);

        repository.save(transaction);
        repository.save(transaction2);
        repository.save(transaction3);

        repository.deleteById(transaction2.getId());

        assertEquals(List.of(transaction, transaction3), repository.findAll());
    }

    @Test
    void shouldDoNothingWhenDeletingTransactionTwice() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);
        Transaction transaction2 = new Transaction(
                "Salary", new BigDecimal("300.00"),
                TransactionType.INCOME,
                LocalDate.of(2025, 1, 1),
                Category.SALARY);
        Transaction transaction3 = new Transaction(
                "Other", new BigDecimal("50.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.OTHER);

        repository.save(transaction);
        repository.save(transaction2);
        repository.save(transaction3);

        repository.deleteById(transaction2.getId());
        repository.deleteById(transaction2.getId());

        assertEquals(List.of(transaction, transaction3), repository.findAll());
    }

    @Test
    void shouldDoNothingWhenDeletingUnknownId() {
        Transaction transaction = new Transaction(
                "Food", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.FOOD);
        Transaction transaction2 = new Transaction(
                "Salary", new BigDecimal("300.00"),
                TransactionType.INCOME,
                LocalDate.of(2025, 1, 1),
                Category.SALARY);
        Transaction transaction3 = new Transaction(
                "Other", new BigDecimal("50.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.OTHER);

        repository.save(transaction);
        repository.save(transaction2);
        repository.save(transaction3);

        repository.deleteById(UUID.randomUUID());

        assertEquals(List.of(transaction, transaction2, transaction3), repository.findAll());
    }

    @Test
    void shouldDoNothingWhenDeletingFromEmptyRepository() {
        repository.deleteById(UUID.randomUUID());

        assertEquals(List.of(), repository.findAll());
    }

    @Test
    void shouldRejectNullIdWhenDeletingTransaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.deleteById(null)
        );
    }
}
