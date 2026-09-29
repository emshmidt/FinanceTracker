package com.learning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void addRejectsDuplicateTransactionId() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);

        transactionService.add(transactionSalary);
        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.add(transactionSalary)
        );

        assertEquals(1, transactionService.findAll().size());
    }

    @Test
    void findByIdReturnsTransactionForExistingId() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        UUID id = transactionSalary.getId();

        transactionService.add(transactionSalary);
        Optional<Transaction> foundTransaction = transactionService.findById(id);

        assertEquals(Optional.of(transactionSalary), foundTransaction);
    }

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        UUID id =UUID.randomUUID();

        transactionService.add(transactionSalary);
        Optional<Transaction> foundTransaction = transactionService.findById(id);

        assertEquals(Optional.empty(), foundTransaction);
    }

    @Test
    void removeByIdRemovesTransactionAndReturnsTrue() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        UUID id = transactionSalary.getId();

        transactionService.add(transactionSalary);
        boolean result = transactionService.removeById(id);

        assertEquals(Optional.empty(), transactionService.findById(id));
        assertEquals(0, transactionService.findAll().size());
        assertTrue(result);
    }

    @Test
    void removeByIdReturnsFalseForUnknownId() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        UUID id = UUID.randomUUID();

        transactionService.add(transactionSalary);
        boolean result = transactionService.removeById(id);

        assertEquals(1, transactionService.findAll().size());
        assertFalse(result);
    }

    @Test
    void findByCategoryReturnsOnlyMatchingTransactions() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);

        transactionService.add(transactionSalary);
        transactionService.add(transactionFood);
        List<Transaction> found = transactionService.findByCategory(Category.SALARY);

        assertEquals(List.of(transactionSalary), found);
    }

    @Test
    void findByDateRangeIncludesBothBoundaries() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 14), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);

        transactionService.add(transactionSalary);
        transactionService.add(transactionFood);
        List<Transaction> found = transactionService.findByDateRange(LocalDate.of(2025, 1, 14), LocalDate.of(2025, 1, 15));

        assertEquals(2, found.size());
    }

    @Test
    void findByDateRangeExcludesTransactionsOutsideRange() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 14), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);
        Transaction transactionFood2 = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 16), Category.FOOD);

        transactionService.add(transactionSalary);
        transactionService.add(transactionFood);
        transactionService.add(transactionFood2);
        List<Transaction> found = transactionService.findByDateRange(LocalDate.of(2025, 1, 14), LocalDate.of(2025, 1, 15));

        assertEquals(List.of(transactionSalary, transactionFood), found);
    }

    @Test
    void findByDateRangeRejectsReversedRange() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 14), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);

        transactionService.add(transactionSalary);
        transactionService.add(transactionFood);

        assertThrows(
                IllegalArgumentException.class,
                () ->  transactionService.findByDateRange(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 14))
        );
    }

    @Test
    void sortByDateReturnsTransactionsInAscendingOrder() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 14), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);

        transactionService.add(transactionFood);
        transactionService.add(transactionSalary);

        List<Transaction> found = transactionService.sortByDate();
        boolean result = found.get(0).getDate().isBefore(found.get(1).getDate());

        assertTrue(result);
    }

    @Test
    void sortByDateDoesNotChangeStoredOrder() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 14), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);

        transactionService.add(transactionFood);
        transactionService.add(transactionSalary);

        transactionService.sortByDate();
        Transaction first = transactionService.findAll().getFirst();

        assertEquals(transactionFood.getId(), first.getId());
    }

    @Test
    void expensesByCategorySumsExpensesInSameCategory(){
        TransactionService transactionService = new TransactionService();
        Transaction firstTransaction = new Transaction("a", new BigDecimal("100.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 14), Category.SALARY);
        Transaction secondTransaction = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.SALARY);

        transactionService.add(firstTransaction);
        transactionService.add(secondTransaction);

        Map<Category, BigDecimal> expenses = transactionService.expensesByCategory();

        BigDecimal sum = expenses.getOrDefault(Category.SALARY, BigDecimal.ZERO);
        boolean result = new BigDecimal("130.00").compareTo(sum) == 0;

        assertTrue(result);
    }

    @Test
    void expensesByCategoryIgnoresIncome() {
        TransactionService transactionService = new TransactionService();
        Transaction firstTransaction = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 14), Category.SALARY);
        Transaction secondTransaction = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.SALARY);

        transactionService.add(firstTransaction);
        transactionService.add(secondTransaction);

        Map<Category, BigDecimal> expenses = transactionService.expensesByCategory();

        BigDecimal sum = expenses.getOrDefault(Category.SALARY, BigDecimal.ZERO);
        boolean result = new BigDecimal("30.00").compareTo(sum) == 0;

        assertTrue(result);
    }

    @Test
    void expensesByCategoryPreservesCategoryInsertionOrder() {
        TransactionService transactionService = new TransactionService();
        Transaction firstTransaction = new Transaction("a", new BigDecimal("100.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 14), Category.SALARY);
        Transaction secondTransaction = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);

        transactionService.add(firstTransaction);
        transactionService.add(secondTransaction);

        Map<Category, BigDecimal> expenses = transactionService.expensesByCategory();
        List<Category> categories = new ArrayList<>(expenses.keySet());

        assertEquals(Category.SALARY, categories.get(0));
        assertEquals(Category.FOOD, categories.get(1));
    }

    @Test
    void findAllReturnsSnapshotUnaffectedByLaterAdd() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);

        transactionService.add(transactionSalary);

        List<Transaction> foundAll = transactionService.findAll();
        transactionService.add(transactionFood);

        assertEquals(1, foundAll.size());
    }

}
