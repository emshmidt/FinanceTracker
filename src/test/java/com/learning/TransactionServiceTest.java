package com.learning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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

        assertEquals(new BigDecimal("0.00"), transactionService.calculateBalance());
    }

    @Test
    void calculateBalanceAddsFractionalAmountsExactly() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("0.1"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("0.2"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.FOOD);

        transactionService.add(transactionSalary);
        transactionService.add(transactionFood);

        assertEquals(new BigDecimal("0.30"), transactionService.calculateBalance());
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
    void findByCategoryReturnsEmptyWhenNoTransactionsMatch() {
        TransactionService service = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.FOOD);

        service.add(transactionFood);
        service.add(transactionSalary);

        assertEquals(List.of(), service.findByCategory(Category.TRANSPORT));
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
        Transaction transactionSalary2 = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 13), Category.SALARY);

        transactionService.add(transactionSalary);
        transactionService.add(transactionFood);
        transactionService.add(transactionFood2);
        transactionService.add(transactionSalary2);

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
    void findByDateRangeReturnsTransactionsOnSingleDay() {
        TransactionService service = new TransactionService();
        LocalDate dateEarlier = LocalDate.of(2025, 1, 14);
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalDate dateLater = LocalDate.of(2025, 1, 16);


        Transaction transactionEarlier = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, dateEarlier, Category.SALARY);
        Transaction transactionDayOf = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, date, Category.FOOD);
        Transaction transactionLater = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, dateLater, Category.FOOD);

        service.add(transactionLater);
        service.add(transactionDayOf);
        service.add(transactionEarlier);

        assertEquals(List.of(transactionDayOf), service.findByDateRange(date, date));
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
    void expensesByCategoryAddsFractionalAmountsExactly() {
        TransactionService transactionService = new TransactionService();
        Transaction firstTransaction = new Transaction("a", new BigDecimal("0.1"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 14), Category.SALARY);
        Transaction secondTransaction = new Transaction("b", new BigDecimal("0.2"), TransactionType.EXPENSE, LocalDate.of(2025, 1, 15), Category.SALARY);

        transactionService.add(firstTransaction);
        transactionService.add(secondTransaction);

        Map<Category, BigDecimal> expenses = transactionService.expensesByCategory();

        assertEquals(new BigDecimal("0.30"), expenses.get(Category.SALARY));
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

    @Test
    void getByIdOrThrowReturnsTransactionForExistingId() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        UUID id = transactionSalary.getId();

        transactionService.add(transactionSalary);
        Transaction result = transactionService.getByIdOrThrow(id);

        assertSame(transactionSalary, result);
    }

    @Test
    void getByIdOrThrowThrowsTransactionNotFoundExceptionForUnknownId() {
        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2025, 1, 15), Category.SALARY);
        UUID id = UUID.randomUUID();

        transactionService.add(transactionSalary);
        assertThrows(
                TransactionNotFoundException.class,
                () ->  transactionService.getByIdOrThrow(id)
        );
    }

    @Test
    void findLargestExpensesReturnsTopExpensesInDescendingOrder() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = new Transaction(
                "Проезд", new BigDecimal("30.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        Transaction expense100 = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.FOOD
        );

        Transaction expense50 = new Transaction(
                "Коммунальные услуги", new BigDecimal("50.00"),
                TransactionType.EXPENSE, date, Category.UTILITIES
        );

        Transaction income1000 = new Transaction(
                "Зарплата", new BigDecimal("1000.00"),
                TransactionType.INCOME, date, Category.SALARY
        );

        service.add(expense30);
        service.add(expense100);
        service.add(expense50);
        service.add(income1000);

        assertEquals(
                List.of(expense100, expense50),
                service.findLargestExpenses(2)
        );
    }

    @Test
    void findLargestExpensesReturnsAllExpensesWhenLimitExceedsCount() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = new Transaction(
                "Проезд", new BigDecimal("30.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        Transaction expense100 = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.FOOD
        );

        Transaction expense50 = new Transaction(
                "Коммунальные услуги", new BigDecimal("50.00"),
                TransactionType.EXPENSE, date, Category.UTILITIES
        );

        Transaction income1000 = new Transaction(
                "Зарплата", new BigDecimal("1000.00"),
                TransactionType.INCOME, date, Category.SALARY
        );

        service.add(expense30);
        service.add(expense100);
        service.add(expense50);
        service.add(income1000);

        assertEquals(
                List.of(expense100, expense50, expense30),
                service.findLargestExpenses(5)
        );
    }

    @Test
    void findLargestExpensesReturnsEmptyListForZeroLimit() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = new Transaction(
                "Проезд", new BigDecimal("30.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        Transaction expense100 = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.FOOD
        );

        Transaction expense50 = new Transaction(
                "Коммунальные услуги", new BigDecimal("50.00"),
                TransactionType.EXPENSE, date, Category.UTILITIES
        );

        Transaction income1000 = new Transaction(
                "Зарплата", new BigDecimal("1000.00"),
                TransactionType.INCOME, date, Category.SALARY
        );

        service.add(expense30);
        service.add(expense100);
        service.add(expense50);
        service.add(income1000);

        assertEquals(
                List.of(),
                service.findLargestExpenses(0)
        );
    }

    @Test
    void findLargestExpensesRejectsNegativeLimit() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = new Transaction(
                "Проезд", new BigDecimal("30.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );
        service.add(expense30);
        assertThrows(
                IllegalArgumentException.class,
                () ->  service.findLargestExpenses(-9)
        );
    }

    @Test
    void findLargestExpensesReturnsEmptyListWhenServiceIsEmpty() {
        TransactionService service = new TransactionService();

        assertEquals(List.of(), service.findLargestExpenses(2));
    }

    @Test
    void findLargestExpensesReturnsEmptyListWhenOnlyIncomeExists() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction income1000 = new Transaction(
                "Зарплата", new BigDecimal("1000.00"),
                TransactionType.INCOME, date, Category.SALARY
        );

        service.add(income1000);

        assertEquals(List.of(), service.findLargestExpenses(2));
    }

    @Test
    void findLargestExpensesSortsEqualAmountsByDateThenId() {
        TransactionService service = new TransactionService();

        Transaction earlier = new Transaction(
                "Проезд", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1), Category.TRANSPORT
        );

        Transaction first = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 2), Category.FOOD
        );

        Transaction second = new Transaction(
                "Услуги", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 2), Category.UTILITIES
        );

        Transaction smallerId =
                first.getId().compareTo(second.getId()) < 0 ? first : second;
        Transaction largerId = smallerId == first ? second : first;

        service.add(largerId);
        service.add(smallerId);
        service.add(earlier);

        List<Transaction> result = service.findLargestExpenses(3);

        assertEquals(
                List.of(earlier, smallerId, largerId),
                result
        );
    }

    @Test
    void findLargestExpensesDoesNotChangeStoredOrder() {
        TransactionService service = new TransactionService();

        Transaction earlier = new Transaction(
                "Проезд", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1), Category.TRANSPORT
        );

        Transaction first = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 2), Category.FOOD
        );

        Transaction second = new Transaction(
                "Услуги", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 2), Category.UTILITIES
        );

        Transaction smallerId =
                first.getId().compareTo(second.getId()) < 0 ? first : second;
        Transaction largerId = smallerId == first ? second : first;

        service.add(largerId);
        service.add(smallerId);
        service.add(earlier);

        service.findLargestExpenses(3);

        assertEquals(
                List.of(largerId, smallerId, earlier),
                service.findAll()
        );
    }

    @Test
    void findMonthWithLargestExpensesSumsExpensesWithinMonth() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);
        LocalDate date2 = LocalDate.of(2026, 9, 1);

        Transaction expense30 = new Transaction(
                "Проезд", new BigDecimal("30.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        Transaction expense80 = new Transaction(
                "Продукты", new BigDecimal("80.00"),
                TransactionType.EXPENSE, date, Category.FOOD
        );

        Transaction expense100 = new Transaction(
                "Коммунальные услуги", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date2, Category.UTILITIES
        );


        service.add(expense30);
        service.add(expense80);
        service.add(expense100);

        assertEquals(Optional.of(YearMonth.from(date)), service.findMonthWithLargestExpenses());
    }

    @Test
    void findMonthWithLargestExpensesIgnoresIncome() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);
        LocalDate date2 = LocalDate.of(2026, 9, 1);

        Transaction expense30 = new Transaction(
                "Проезд", new BigDecimal("30.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        Transaction expense100 = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.FOOD
        );

        Transaction expense50 = new Transaction(
                "Коммунальные услуги", new BigDecimal("50.00"),
                TransactionType.EXPENSE, date, Category.UTILITIES
        );

        Transaction income1000 = new Transaction(
                "Зарплата", new BigDecimal("1000.00"),
                TransactionType.INCOME, date2, Category.SALARY
        );

        service.add(expense30);
        service.add(expense100);
        service.add(expense50);
        service.add(income1000);

        assertEquals(Optional.of(YearMonth.from(date)), service.findMonthWithLargestExpenses());
    }

    @Test
    void findMonthWithLargestExpensesDistinguishesSameMonthInDifferentYears() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);
        LocalDate date2 = LocalDate.of(2026, 9, 1);
        LocalDate dateEarlier = LocalDate.of(2025, 10, 2);

        Transaction expense30 = new Transaction(
                "Проезд", new BigDecimal("30.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        Transaction expense80 = new Transaction(
                "Продукты", new BigDecimal("80.00"),
                TransactionType.EXPENSE, dateEarlier, Category.FOOD
        );

        Transaction expense100 = new Transaction(
                "Коммунальные услуги", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date2, Category.UTILITIES
        );


        service.add(expense30);
        service.add(expense80);
        service.add(expense100);

        assertEquals(Optional.of(YearMonth.from(date2)), service.findMonthWithLargestExpenses());
    }

    @Test
    void findMonthWithLargestExpensesReturnsEarlierMonthWhenTotalsAreEqual() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);
        LocalDate laterDate = LocalDate.of(2026, 11, 3);

        Transaction expenseFirst = new Transaction(
                "Проезд", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        Transaction expenseSecond = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE, laterDate, Category.FOOD
        );


        service.add(expenseSecond);
        service.add(expenseFirst);

        assertEquals(Optional.of(YearMonth.from(date)), service.findMonthWithLargestExpenses());
    }

    @Test
    void findMonthWithLargestExpensesReturnsEmptyWhenServiceIsEmpty() {
        TransactionService service = new TransactionService();

        assertEquals(Optional.empty(), service.findMonthWithLargestExpenses());
    }

    @Test
    void findMonthWithLargestExpensesReturnsEmptyWhenOnlyIncomeExists() {
        TransactionService service = new TransactionService();
        Transaction income1000 = new Transaction(
                "Зарплата", new BigDecimal("1000.00"),
                TransactionType.INCOME, LocalDate.of(2025, 1, 1), Category.SALARY
        );

        service.add(income1000);

        assertEquals(Optional.empty(), service.findMonthWithLargestExpenses());
    }

    @Test
    void findMostExpensiveCategoryReturnsCategoryWithLargestTotal() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = new Transaction(
                "Проезд", new BigDecimal("30.00"),
                TransactionType.EXPENSE, date, Category.FOOD
        );

        Transaction expense80 = new Transaction(
                "Продукты", new BigDecimal("80.00"),
                TransactionType.EXPENSE, date, Category.FOOD
        );

        Transaction expense100 = new Transaction(
                "Коммунальные услуги", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        service.add(expense30);
        service.add(expense100);
        service.add(expense80);

        assertEquals(Optional.of(Category.FOOD), service.findMostExpensiveCategory());
    }

    @Test
    void findMostExpensiveCategoryReturnsAlphabeticallyFirstNameWhenTotalsAreEqual() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseRent = new Transaction(
                "Коммунальные услуги", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.RENT
        );
        Transaction expenseTransport = new Transaction(
                "Коммунальные услуги", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        service.add(expenseTransport);
        service.add(expenseRent);

        assertEquals(Optional.of(Category.RENT), service.findMostExpensiveCategory());
    }

    @Test
    void findMostExpensiveCategoryReturnsEmptyWhenServiceIsEmpty() {
        TransactionService service = new TransactionService();

        assertEquals(Optional.empty(), service.findMostExpensiveCategory());
    }

    @Test
    void findMostExpensiveCategoryReturnsEmptyWhenOnlyIncomeExists() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction income = new Transaction(
                "Коммунальные услуги", new BigDecimal("100.00"),
                TransactionType.INCOME, date, Category.SALARY
        );

        service.add(income);

        assertEquals(Optional.empty(), service.findMostExpensiveCategory());
    }

    @Test
    void calculateAverageExpenseReturnsAverageOfExpenses() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseFood = new Transaction(
                "Коммунальные услуги", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.FOOD
        );
        Transaction expenseTransport = new Transaction(
                "Коммунальные услуги", new BigDecimal("200.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        service.add(expenseTransport);
        service.add(expenseFood);

        assertEquals(Optional.of(new BigDecimal("150.00")), service.calculateAverageExpense());
    }

    @Test
    void calculateAverageExpenseIgnoresIncome() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseFood = new Transaction(
                "Коммунальные услуги", new BigDecimal("100.00"),
                TransactionType.EXPENSE, date, Category.FOOD
        );
        Transaction expenseTransport = new Transaction(
                "Коммунальные услуги", new BigDecimal("200.00"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );
        Transaction income = new Transaction(
                "Коммунальные услуги", new BigDecimal("1000.00"),
                TransactionType.INCOME, date, Category.FOOD
        );

        service.add(expenseTransport);
        service.add(expenseFood);
        service.add(income);

        assertEquals(Optional.of(new BigDecimal("150.00")), service.calculateAverageExpense());
    }

    @Test
    void calculateAverageExpenseRoundsToTwoDecimalPlacesUsingHalfUp() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseFood = new Transaction(
                "Коммунальные услуги", new BigDecimal("0.10"),
                TransactionType.EXPENSE, date, Category.FOOD
        );
        Transaction expenseTransport = new Transaction(
                "Коммунальные услуги", new BigDecimal("0.11"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );

        service.add(expenseTransport);
        service.add(expenseFood);

        assertEquals(Optional.of(new BigDecimal("0.11")), service.calculateAverageExpense());
    }

    @Test
    void calculateAverageExpenseHandlesNonTerminatingDivision() {
        TransactionService service = new TransactionService();
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseFood = new Transaction(
                "Коммунальные услуги", new BigDecimal("0.10"),
                TransactionType.EXPENSE, date, Category.FOOD
        );
        Transaction expenseTransport = new Transaction(
                "Коммунальные услуги", new BigDecimal("0.20"),
                TransactionType.EXPENSE, date, Category.TRANSPORT
        );
        Transaction expenseUtilities = new Transaction(
                "Коммунальные услуги", new BigDecimal("0.20"),
                TransactionType.EXPENSE, date, Category.UTILITIES
        );

        service.add(expenseTransport);
        service.add(expenseFood);
        service.add(expenseUtilities);

        assertEquals(Optional.of(new BigDecimal("0.17")), service.calculateAverageExpense());
    }

    @Test
    void calculateAverageExpenseReturnsEmptyWhenServiceIsEmpty() {
        TransactionService service = new TransactionService();

        assertEquals(Optional.empty(), service.calculateAverageExpense());
    }

   @Test
   void calculateAverageExpenseReturnsEmptyWhenOnlyIncomeExists() {
        TransactionService service = new TransactionService();
        Transaction income = new Transaction("Salary", new BigDecimal("100.00"),
                TransactionType.INCOME, LocalDate.of(2025, 1, 1),
                Category.SALARY);

        service.add(income);

       assertEquals(Optional.empty(), service.calculateAverageExpense());
   }
}
