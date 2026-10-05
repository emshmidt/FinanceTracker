package com.learning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionServiceTest {
    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService();
    }

    @Test
    void shouldCalculateBalanceFromIncomeAndExpenses() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryIncome, foodExpense);

        assertEquals(new BigDecimal("70.00"), service.calculateBalance());
    }

    @Test
    void shouldReturnZeroBalanceWhenNoTransactionsExist() {
        assertEquals(new BigDecimal("0.00"), service.calculateBalance());
    }

    @Test
    void shouldCalculateBalanceWithFractionalAmountsExactly() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "0.1", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodIncome = createTransaction(
                "Возврат за продукты", "0.2", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryIncome, foodIncome);

        assertEquals(new BigDecimal("0.30"), service.calculateBalance());
    }

    @Test
    void shouldReturnNegativeBalanceWhenOnlyExpensesExist() {
        Transaction salaryExpense = createTransaction(
                "Зарплатные выплаты", "0.1", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "0.2", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryExpense, foodExpense);

        assertEquals(new BigDecimal("-0.30"), service.calculateBalance());
    }

    @Test
    void shouldUpdateBalanceAfterRemovingExpense() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "0.1", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "0.2", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryIncome, foodExpense);
        service.removeById(foodExpense.getId());

        assertEquals(salaryIncome.getAmount(), service.calculateBalance());
    }

    @Test
    void shouldReturnUnmodifiableListWhenFindingAllTransactions() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        service.add(salaryIncome);

        List<Transaction> transactionList = service.findAll();

        assertThrows(
                UnsupportedOperationException.class,
                () -> transactionList.add(createTransaction(
                        "Продукты", "30.00", TransactionType.EXPENSE,
                        LocalDate.of(2025, 1, 15), Category.FOOD
                ))
        );
        assertEquals(List.of(salaryIncome), service.findAll());
    }

    @Test
    void shouldRejectAddingTransactionWithDuplicateId() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        service.add(salaryIncome);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.add(salaryIncome)
        );

        assertEquals(List.of(salaryIncome), service.findAll());
    }

    @Test
    void shouldAddValidTransaction() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        service.add(salaryIncome);

        assertEquals(List.of(salaryIncome), service.findAll());
    }

    @Test
    void shouldRejectNullTransactionWithoutChangingState() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.add(null)
        );

        assertEquals(List.of(), service.findAll());
    }

    @Test
    void shouldRejectNullIdWhenFindingTransaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.findById(null)
        );
    }

    @Test
    void shouldRejectNullIdWhenGettingTransaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getByIdOrThrow(null)
        );
    }

    @Test
    void shouldFindTransactionWhenIdExists() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        UUID id = salaryIncome.getId();

        service.add(salaryIncome);
        Optional<Transaction> foundTransaction = service.findById(id);

        assertEquals(Optional.of(salaryIncome), foundTransaction);
    }

    @Test
    void shouldReturnEmptyOptionalWhenFindingUnknownTransaction() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        UUID id = UUID.randomUUID();

        service.add(salaryIncome);
        Optional<Transaction> foundTransaction = service.findById(id);

        assertEquals(Optional.empty(), foundTransaction);
    }

    @Test
    void shouldRemoveTransactionAndReturnTrueWhenIdExists() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        UUID id = salaryIncome.getId();

        service.add(salaryIncome);
        boolean result = service.removeById(id);

        assertEquals(Optional.empty(), service.findById(id));
        assertEquals(List.of(), service.findAll());
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenRemovingUnknownTransaction() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        UUID id = UUID.randomUUID();

        service.add(salaryIncome);
        boolean result = service.removeById(id);

        assertEquals(List.of(salaryIncome), service.findAll());
        assertFalse(result);
    }

    @Test
    void shouldRemoveOnlyTransactionWithMatchingId() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "0.1", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "0.2", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryIncome, foodExpense);
        service.removeById(foodExpense.getId());

        assertEquals(List.of(salaryIncome), service.findAll());
    }

    @Test
    void shouldRejectNullIdWhenRemovingTransaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.removeById(null)
        );
    }

    @Test
    void shouldReturnFalseWhenRemovingSameTransactionTwice() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "0.1", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "0.2", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryIncome, foodExpense);
        service.removeById(foodExpense.getId());

        boolean result = service.removeById(foodExpense.getId());

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenRemovingTransactionFromEmptyService() {
        UUID id = UUID.randomUUID();

        boolean removed = service.removeById(id);

        assertFalse(removed);
        assertEquals(List.of(), service.findAll());
    }

    @Test
    void shouldPreserveSnapshotWhenTransactionIsRemovedLater() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "0.1", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "0.2", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryIncome, foodExpense);
        List<Transaction> snapshot = service.findAll();

        service.removeById(foodExpense.getId());
        List<Transaction> currentTransactions = service.findAll();

        assertEquals(List.of(salaryIncome, foodExpense), snapshot);
        assertEquals(List.of(salaryIncome), currentTransactions);
    }

    @Test
    void shouldFindOnlyTransactionsInRequestedCategory() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        Transaction foodIncome = createTransaction(
                "Возврат за продукты", "30.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryIncome, foodExpense, foodIncome);
        List<Transaction> found = service.findByCategory(Category.FOOD);

        assertEquals(List.of(foodExpense, foodIncome), found);
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsMatchCategory() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(foodExpense, salaryIncome);

        assertEquals(List.of(), service.findByCategory(Category.TRANSPORT));
    }

    @Test
    void shouldRejectNullCategory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.findByCategory(null)
        );
    }

    @Test
    void shouldReturnUnmodifiableListWhenFindingTransactionsByCategory() {
        Transaction transportIncome = createTransaction(
                "Компенсация проезда", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.TRANSPORT
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(foodExpense, transportIncome);

        List<Transaction> found = service.findByCategory(Category.TRANSPORT);

        assertThrows(
                UnsupportedOperationException.class,
                () -> found.add(createTransaction(
                        "Коммунальные услуги", "30.00", TransactionType.EXPENSE,
                        LocalDate.of(2025, 1, 15), Category.UTILITIES
                ))
        );
        assertEquals(List.of(transportIncome), found);
        assertEquals(List.of(foodExpense, transportIncome), service.findAll());
    }

    @ParameterizedTest
    @MethodSource("dateRangesWithNullBoundaries")
    void shouldRejectDateRangeWithNullBoundary(
            LocalDate from,
            LocalDate to
    ) {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.findByDateRange(from, to)
        );
    }

    static Stream<Arguments> dateRangesWithNullBoundaries() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        return Stream.of(
                Arguments.of(null, date),
                Arguments.of(date, null),
                Arguments.of(null, null)
        );
    }

    @Test
    void shouldIncludeBothBoundariesWhenFindingTransactionsByDateRange() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryIncome, foodExpense);
        List<Transaction> found = service.findByDateRange(LocalDate.of(2025, 1, 14), LocalDate.of(2025, 1, 15));

        assertEquals(List.of(salaryIncome, foodExpense), found);
    }

    @Test
    void shouldExcludeTransactionsOutsideRequestedDateRange() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        Transaction laterFoodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 16), Category.FOOD
        );
        Transaction earlierSalaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 13), Category.SALARY
        );

        addTransactions(salaryIncome, foodExpense, laterFoodExpense, earlierSalaryIncome);

        List<Transaction> found = service.findByDateRange(LocalDate.of(2025, 1, 14), LocalDate.of(2025, 1, 15));

        assertEquals(List.of(salaryIncome, foodExpense), found);
    }

    @Test
    void shouldRejectReversedDateRange() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(salaryIncome, foodExpense);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.findByDateRange(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 14))
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoDatesMatch() {
        Transaction laterFoodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 16), Category.FOOD
        );
        Transaction earlierSalaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 13), Category.SALARY
        );

        addTransactions(laterFoodExpense, earlierSalaryIncome);

        List<Transaction> found = service.findByDateRange(LocalDate.of(2025, 1, 14), LocalDate.of(2025, 1, 15));

        assertEquals(List.of(), found);
    }

    @Test
    void shouldSortTransactionsByDateInAscendingOrder() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(foodExpense, salaryIncome);

        List<Transaction> found = service.sortByDate();
        assertEquals(List.of(salaryIncome, foodExpense), found);
    }

    @Test
    void shouldPreserveStoredOrderWhenSortingTransactionsByDate() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(foodExpense, salaryIncome);

        service.sortByDate();
        assertEquals(List.of(foodExpense, salaryIncome), service.findAll());
    }

    @Test
    void shouldReturnEmptyListWhenSortingEmptyService() {
        List<Transaction> sorted = service.sortByDate();

        assertEquals(List.of(), sorted);
    }

    @Test
    void shouldReturnSingleTransactionWhenSorting() {
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        service.add(foodExpense);

        List<Transaction> sorted = service.sortByDate();

        assertEquals(List.of(foodExpense), sorted);
    }

    @Test
    void shouldFindTransactionsOnSingleDayWhenDateBoundariesAreEqual() {
        LocalDate dateEarlier = LocalDate.of(2025, 1, 14);
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalDate dateLater = LocalDate.of(2025, 1, 16);

        Transaction earlierSalaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                dateEarlier, Category.SALARY
        );
        Transaction foodExpenseOnDate = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );
        Transaction laterFoodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                dateLater, Category.FOOD
        );

        addTransactions(laterFoodExpense, foodExpenseOnDate, earlierSalaryIncome);

        assertEquals(List.of(foodExpenseOnDate), service.findByDateRange(date, date));
    }

    @Test
    void shouldReturnUnmodifiableListWhenFindingTransactionsByDateRange() {
        LocalDate dateEarlier = LocalDate.of(2025, 1, 14);
        LocalDate date = LocalDate.of(2025, 1, 15);

        Transaction earlierSalaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                dateEarlier, Category.SALARY
        );
        Transaction foodExpenseOnDate = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );

        addTransactions(foodExpenseOnDate, earlierSalaryIncome);

        List<Transaction> found = service.findByDateRange(dateEarlier, date);

        assertThrows(
                UnsupportedOperationException.class,
                () -> found.add(createTransaction(
                        "Продукты", "30.00", TransactionType.EXPENSE,
                        LocalDate.of(2025, 1, 1), Category.FOOD
                ))
        );
        assertEquals(List.of(foodExpenseOnDate, earlierSalaryIncome), found);
        assertEquals(List.of(foodExpenseOnDate, earlierSalaryIncome), service.findAll());
    }

    @Test
    void shouldReturnEmptyExpenseTotalsWhenNoTransactionsExist() {
        Map<Category, BigDecimal> totals = service.expensesByCategory();

        assertEquals(Map.of(), totals);
    }

    @Test
    void shouldSumExpensesInSameCategory() {
        Transaction firstSalaryExpense = createTransaction(
                "Зарплатные выплаты", "100.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction secondSalaryExpense = createTransaction(
                "Зарплатные выплаты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        addTransactions(firstSalaryExpense, secondSalaryExpense);

        Map<Category, BigDecimal> expenses = service.expensesByCategory();

        assertEquals(Map.of(Category.SALARY, new BigDecimal("130.00")), expenses);
    }

    @Test
    void shouldIgnoreIncomeWhenGroupingExpensesByCategory() {
        Transaction firstSalaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction secondSalaryExpense = createTransaction(
                "Зарплатные выплаты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        addTransactions(firstSalaryIncome, secondSalaryExpense);

        Map<Category, BigDecimal> expenses = service.expensesByCategory();

        assertEquals(Map.of(Category.SALARY, new BigDecimal("30.00")), expenses);
    }

    @Test
    void shouldPreserveCategoryInsertionOrderWhenGroupingExpenses() {
        Transaction firstSalaryExpense = createTransaction(
                "Зарплатные выплаты", "100.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction secondFoodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(firstSalaryExpense, secondFoodExpense);

        Map<Category, BigDecimal> expenses = service.expensesByCategory();
        List<Category> categories = new ArrayList<>(expenses.keySet());

        assertEquals(List.of(Category.SALARY, Category.FOOD), categories);
    }

    @Test
    void shouldSumFractionalExpensesByCategoryExactly() {
        Transaction firstSalaryExpense = createTransaction(
                "Зарплатные выплаты", "0.1", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction secondSalaryExpense = createTransaction(
                "Зарплатные выплаты", "0.2", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        addTransactions(firstSalaryExpense, secondSalaryExpense);

        Map<Category, BigDecimal> expenses = service.expensesByCategory();

        assertEquals(Map.of(Category.SALARY, new BigDecimal("0.30")), expenses);
    }

    @Test
    void shouldReturnEmptyExpenseTotalsWhenOnlyIncomeExists() {
        Transaction transportIncome = createTransaction(
                "Компенсация проезда", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.TRANSPORT
        );
        Transaction salaryIncome = createTransaction(
                "Зарплата", "30.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        addTransactions(salaryIncome, transportIncome);

        assertEquals(Map.of(), service.expensesByCategory());
    }

    @Test
    void shouldCalculateExpenseTotalsForMultipleCategories() {
        LocalDate date = LocalDate.of(2025, 1, 15);

        service.add(createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                date, Category.FOOD
        ));
        service.add(createTransaction(
                "Обед", "20.00", TransactionType.EXPENSE,
                date, Category.FOOD
        ));
        service.add(createTransaction(
                "Аренда", "70.00", TransactionType.EXPENSE,
                date, Category.RENT
        ));
        service.add(createTransaction(
                "Возврат денег", "100.00", TransactionType.INCOME,
                date, Category.FOOD
        ));

        Map<Category, BigDecimal> totals = service.expensesByCategory();

        assertEquals(
                Map.of(
                        Category.FOOD, new BigDecimal("50.00"),
                        Category.RENT, new BigDecimal("70.00")
                ),
                totals
        );
    }

    @Test
    void shouldPreserveExpenseTotalsWhenReturnedMapIsModified() {
        Transaction salaryExpense = createTransaction(
                "Зарплатные выплаты", "100.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction salaryExpense2 = createTransaction(
                "Зарплатные выплаты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        addTransactions(salaryExpense2, salaryExpense);

        Map<Category, BigDecimal> returnedTotals = service.expensesByCategory();

        returnedTotals.clear();

        Map<Category, BigDecimal> recalculatedTotals = service.expensesByCategory();

        assertEquals(Map.of(Category.SALARY, new BigDecimal("130.00")), recalculatedTotals);
    }

    @Test
    void shouldPreserveSnapshotWhenTransactionIsAddedLater() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        service.add(salaryIncome);

        List<Transaction> snapshot = service.findAll();
        service.add(foodExpense);

        assertEquals(List.of(salaryIncome), snapshot);
        assertEquals(List.of(salaryIncome, foodExpense), service.findAll());
    }

    @Test
    void shouldReturnTransactionWithoutThrowingWhenIdExists() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        UUID id = salaryIncome.getId();

        service.add(salaryIncome);
        Transaction result = service.getByIdOrThrow(id);

        assertSame(salaryIncome, result);
    }

    @Test
    void shouldThrowTransactionNotFoundExceptionWhenIdDoesNotExist() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        UUID id = UUID.randomUUID();

        service.add(salaryIncome);
        assertThrows(
                TransactionNotFoundException.class,
                () -> service.getByIdOrThrow(id)
        );
    }

    @Test
    void shouldReturnLargestExpensesInDescendingOrder() {
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = createTransaction(
                "Проезд", "30.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        Transaction expense100 = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );

        Transaction expense50 = createTransaction(
                "Коммунальные услуги", "50.00", TransactionType.EXPENSE,
                date, Category.UTILITIES
        );

        Transaction income1000 = createTransaction(
                "Зарплата", "1000.00", TransactionType.INCOME,
                date, Category.SALARY
        );

        addTransactions(expense30, expense100, expense50, income1000);

        assertEquals(
                List.of(expense100, expense50),
                service.findLargestExpenses(2)
        );
    }

    @Test
    void shouldReturnAllExpensesWhenLimitExceedsExpenseCount() {
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = createTransaction(
                "Проезд", "30.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        Transaction expense100 = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );

        Transaction expense50 = createTransaction(
                "Коммунальные услуги", "50.00", TransactionType.EXPENSE,
                date, Category.UTILITIES
        );

        Transaction income1000 = createTransaction(
                "Зарплата", "1000.00", TransactionType.INCOME,
                date, Category.SALARY
        );

        addTransactions(expense30, expense100, expense50, income1000);

        assertEquals(
                List.of(expense100, expense50, expense30),
                service.findLargestExpenses(5)
        );
    }

    @Test
    void shouldReturnEmptyListOfLargestExpensesWhenLimitIsZero() {
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = createTransaction(
                "Проезд", "30.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        Transaction expense100 = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );

        Transaction expense50 = createTransaction(
                "Коммунальные услуги", "50.00", TransactionType.EXPENSE,
                date, Category.UTILITIES
        );

        Transaction income1000 = createTransaction(
                "Зарплата", "1000.00", TransactionType.INCOME,
                date, Category.SALARY
        );

        addTransactions(expense30, expense100, expense50, income1000);

        assertEquals(
                List.of(),
                service.findLargestExpenses(0)
        );
    }

    @Test
    void shouldRejectNegativeLimitWhenFindingLargestExpenses() {
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = createTransaction(
                "Проезд", "30.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );
        service.add(expense30);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.findLargestExpenses(-9)
        );
    }

    @Test
    void shouldReturnEmptyListOfLargestExpensesWhenNoTransactionsExist() {
        assertEquals(List.of(), service.findLargestExpenses(2));
    }

    @Test
    void shouldReturnEmptyListOfLargestExpensesWhenOnlyIncomeExists() {
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction income1000 = createTransaction(
                "Зарплата", "1000.00", TransactionType.INCOME,
                date, Category.SALARY
        );

        service.add(income1000);

        assertEquals(List.of(), service.findLargestExpenses(2));
    }

    @Test
    void shouldSortLargestExpensesWithEqualAmountsByDateThenId() {
        Transaction earlier = createTransaction(
                "Проезд", "100.00", TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1), Category.TRANSPORT
        );

        Transaction first = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 2), Category.FOOD
        );

        Transaction second = createTransaction(
                "Услуги", "100.00", TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 2), Category.UTILITIES
        );

        Transaction smallerId =
                first.getId().compareTo(second.getId()) < 0 ? first : second;
        Transaction largerId = smallerId == first ? second : first;

        addTransactions(largerId, smallerId, earlier);

        List<Transaction> result = service.findLargestExpenses(3);

        assertEquals(
                List.of(earlier, smallerId, largerId),
                result
        );
    }

    @Test
    void shouldSortEqualDatesById() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        LocalDate dateLater = LocalDate.of(2025, 1, 16);

        Transaction salaryIncomeOnDate = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                date, Category.SALARY
        );
        Transaction secondFoodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );
        Transaction laterFoodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                dateLater, Category.FOOD
        );

        Transaction first = (salaryIncomeOnDate.getId().compareTo(secondFoodExpense.getId()) < 0) ? salaryIncomeOnDate : secondFoodExpense;

        Transaction second = first.equals(salaryIncomeOnDate) ? secondFoodExpense : salaryIncomeOnDate;

        addTransactions(laterFoodExpense, second, first);

        assertEquals(
                List.of(first, second, laterFoodExpense),
                service.sortByDate()
        );
    }

    @Test
    void shouldReturnUnmodifiableListWhenSortingTransactionsByDate() {
        Transaction salaryIncome = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 14), Category.SALARY
        );
        Transaction foodExpense = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        addTransactions(foodExpense, salaryIncome);

        List<Transaction> sorted = service.sortByDate();

        assertThrows(
                UnsupportedOperationException.class,
                () -> sorted.add(createTransaction(
                        "Зарплата", "100.00", TransactionType.INCOME,
                        LocalDate.of(2025, 1, 14), Category.SALARY
                ))
        );
        assertEquals(List.of(salaryIncome, foodExpense), sorted);
        assertEquals(List.of(foodExpense, salaryIncome), service.findAll());
    }

    @Test
    void shouldPreserveStoredOrderWhenFindingLargestExpenses() {
        Transaction earlier = createTransaction(
                "Проезд", "100.00", TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1), Category.TRANSPORT
        );

        Transaction first = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 2), Category.FOOD
        );

        Transaction second = createTransaction(
                "Услуги", "100.00", TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 2), Category.UTILITIES
        );

        Transaction smallerId =
                first.getId().compareTo(second.getId()) < 0 ? first : second;
        Transaction largerId = smallerId == first ? second : first;

        addTransactions(largerId, smallerId, earlier);

        service.findLargestExpenses(3);

        assertEquals(
                List.of(largerId, smallerId, earlier),
                service.findAll()
        );
    }

    @Test
    void shouldReturnUnmodifiableListWhenFindingLargestExpenses() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expense100 = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );

        Transaction expense50 = createTransaction(
                "Коммунальные услуги", "50.00", TransactionType.EXPENSE,
                date, Category.UTILITIES
        );

        Transaction income1000 = createTransaction(
                "Зарплата", "1000.00", TransactionType.INCOME,
                date, Category.SALARY
        );

        addTransactions(expense100, expense50, income1000);

        List<Transaction> found = service.findLargestExpenses(1);

        assertThrows(
                UnsupportedOperationException.class,
                () -> found.add(createTransaction(
                        "Коммунальные услуги", "50.00", TransactionType.EXPENSE,
                        date, Category.UTILITIES
                ))
        );
        assertEquals(List.of(expense100), found);
        assertEquals(List.of(expense100, expense50, income1000), service.findAll());
    }

    @Test
    void shouldFindMonthWithLargestTotalExpenses() {
        LocalDate date = LocalDate.of(2026, 10, 2);
        LocalDate previousMonthDate = LocalDate.of(2026, 9, 1);

        Transaction expense30 = createTransaction(
                "Проезд", "30.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        Transaction expense80 = createTransaction(
                "Продукты", "80.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );

        Transaction expense100 = createTransaction(
                "Коммунальные услуги", "100.00", TransactionType.EXPENSE,
                previousMonthDate, Category.UTILITIES
        );

        addTransactions(expense30, expense80, expense100);

        assertEquals(Optional.of(YearMonth.from(date)), service.findMonthWithLargestExpenses());
    }

    @Test
    void shouldIgnoreIncomeWhenFindingMonthWithLargestExpenses() {
        LocalDate date = LocalDate.of(2026, 10, 2);
        LocalDate previousMonthDate = LocalDate.of(2026, 9, 1);

        Transaction expense30 = createTransaction(
                "Проезд", "30.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        Transaction expense100 = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );

        Transaction expense50 = createTransaction(
                "Коммунальные услуги", "50.00", TransactionType.EXPENSE,
                date, Category.UTILITIES
        );

        Transaction income1000 = createTransaction(
                "Зарплата", "1000.00", TransactionType.INCOME,
                previousMonthDate, Category.SALARY
        );

        addTransactions(expense30, expense100, expense50, income1000);

        assertEquals(Optional.of(YearMonth.from(date)), service.findMonthWithLargestExpenses());
    }

    @Test
    void shouldDistinguishSameMonthInDifferentYearsWhenFindingLargestExpenses() {
        LocalDate date = LocalDate.of(2026, 10, 2);
        LocalDate previousMonthDate = LocalDate.of(2026, 9, 1);
        LocalDate sameMonthPreviousYear = LocalDate.of(2025, 10, 2);

        Transaction expense30 = createTransaction(
                "Проезд", "30.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        Transaction expense80 = createTransaction(
                "Продукты", "80.00", TransactionType.EXPENSE,
                sameMonthPreviousYear, Category.FOOD
        );

        Transaction expense100 = createTransaction(
                "Коммунальные услуги", "100.00", TransactionType.EXPENSE,
                previousMonthDate, Category.UTILITIES
        );

        addTransactions(expense30, expense80, expense100);

        assertEquals(Optional.of(YearMonth.from(previousMonthDate)), service.findMonthWithLargestExpenses());
    }

    @Test
    void shouldReturnEarlierMonthWhenExpenseTotalsAreEqual() {
        LocalDate date = LocalDate.of(2026, 10, 2);
        LocalDate laterDate = LocalDate.of(2026, 11, 3);

        Transaction expenseFirst = createTransaction(
                "Проезд", "100.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        Transaction expenseSecond = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                laterDate, Category.FOOD
        );

        addTransactions(expenseSecond, expenseFirst);

        assertEquals(Optional.of(YearMonth.from(date)), service.findMonthWithLargestExpenses());
    }

    @Test
    void shouldReturnEmptyMonthWhenNoTransactionsExist() {
        assertEquals(Optional.empty(), service.findMonthWithLargestExpenses());
    }

    @Test
    void shouldReturnEmptyMonthWhenOnlyIncomeExists() {
        Transaction income1000 = createTransaction(
                "Зарплата", "1000.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 1), Category.SALARY
        );

        service.add(income1000);

        assertEquals(Optional.empty(), service.findMonthWithLargestExpenses());
    }

    @Test
    void shouldFindCategoryWithLargestTotalExpenses() {
        LocalDate date = LocalDate.of(2026, 10, 2);

        Transaction expense30 = createTransaction(
                "Продукты", "30.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );

        Transaction expense80 = createTransaction(
                "Продукты", "80.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );

        Transaction expense100 = createTransaction(
                "Проезд", "100.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        addTransactions(expense30, expense100, expense80);

        assertEquals(Optional.of(Category.FOOD), service.findMostExpensiveCategory());
    }

    @Test
    void shouldReturnAlphabeticallyFirstCategoryWhenExpenseTotalsAreEqual() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseRent = createTransaction(
                "Аренда", "100.00", TransactionType.EXPENSE,
                date, Category.RENT
        );
        Transaction expenseTransport = createTransaction(
                "Проезд", "100.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        addTransactions(expenseTransport, expenseRent);

        assertEquals(Optional.of(Category.RENT), service.findMostExpensiveCategory());
    }

    @Test
    void shouldReturnEmptyCategoryWhenNoTransactionsExist() {
        assertEquals(Optional.empty(), service.findMostExpensiveCategory());
    }

    @Test
    void shouldReturnEmptyCategoryWhenOnlyIncomeExists() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction income = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                date, Category.SALARY
        );

        service.add(income);

        assertEquals(Optional.empty(), service.findMostExpensiveCategory());
    }

    @Test
    void shouldCalculateAverageExpense() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseFood = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );
        Transaction expenseTransport = createTransaction(
                "Проезд", "200.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        addTransactions(expenseTransport, expenseFood);

        assertEquals(Optional.of(new BigDecimal("150.00")), service.calculateAverageExpense());
    }

    @Test
    void shouldIgnoreIncomeWhenCalculatingAverageExpense() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseFood = createTransaction(
                "Продукты", "100.00", TransactionType.EXPENSE,
                date, Category.FOOD
        );
        Transaction expenseTransport = createTransaction(
                "Проезд", "200.00", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );
        Transaction income = createTransaction(
                "Возврат за продукты", "1000.00", TransactionType.INCOME,
                date, Category.FOOD
        );

        addTransactions(expenseTransport, expenseFood, income);

        assertEquals(Optional.of(new BigDecimal("150.00")), service.calculateAverageExpense());
    }

    @Test
    void shouldRoundAverageExpenseToTwoDecimalPlacesUsingHalfUp() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseFood = createTransaction(
                "Продукты", "0.10", TransactionType.EXPENSE,
                date, Category.FOOD
        );
        Transaction expenseTransport = createTransaction(
                "Проезд", "0.11", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );

        addTransactions(expenseTransport, expenseFood);

        assertEquals(Optional.of(new BigDecimal("0.11")), service.calculateAverageExpense());
    }

    @Test
    void shouldCalculateAverageExpenseWhenDivisionIsNonTerminating() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        Transaction expenseFood = createTransaction(
                "Продукты", "0.10", TransactionType.EXPENSE,
                date, Category.FOOD
        );
        Transaction expenseTransport = createTransaction(
                "Проезд", "0.20", TransactionType.EXPENSE,
                date, Category.TRANSPORT
        );
        Transaction expenseUtilities = createTransaction(
                "Коммунальные услуги", "0.20", TransactionType.EXPENSE,
                date, Category.UTILITIES
        );

        addTransactions(expenseTransport, expenseFood, expenseUtilities);

        assertEquals(Optional.of(new BigDecimal("0.17")), service.calculateAverageExpense());
    }

    @Test
    void shouldReturnEmptyAverageExpenseWhenNoTransactionsExist() {
        assertEquals(Optional.empty(), service.calculateAverageExpense());
    }

    @Test
    void shouldReturnEmptyAverageExpenseWhenOnlyIncomeExists() {
        Transaction income = createTransaction(
                "Зарплата", "100.00", TransactionType.INCOME,
                LocalDate.of(2025, 1, 1), Category.SALARY
        );

        service.add(income);

        assertEquals(Optional.empty(), service.calculateAverageExpense());
    }

    private Transaction createTransaction(
            String title,
            String amount,
            TransactionType type,
            LocalDate date,
            Category category
    ) {
        return new Transaction(title, new BigDecimal(amount), type, date, category);
    }

    private void addTransactions(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            service.add(transaction);
        }
    }
}
