package com.learning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class TransactionService {
    private static final Predicate<Transaction> IS_EXPENSE =
            t -> t.getType() == TransactionType.EXPENSE;

    private final List<Transaction> transactions = new ArrayList<>();

    public void add(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction must not be null");
        }

        if (findById(transaction.getId()).isPresent()) {
            throw new IllegalArgumentException("Transaction with this ID already exists");
        }

        transactions.add(transaction);
    }

    public List<Transaction> findAll() {
        return List.copyOf(transactions);
    }

    public BigDecimal calculateBalance() {
        BigDecimal balance = new BigDecimal("0.00");
        for (Transaction transaction: transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                balance = balance.add(transaction.getAmount());
            } else {
                balance = balance.subtract(transaction.getAmount());
            }
        }
        return balance;
    }

    public Optional<Transaction> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        return transactions.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    public Transaction getByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(
                () -> new TransactionNotFoundException("Transaction not found: " + id)
        );
    }

    public boolean removeById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID must not be null");
        }
        Iterator<Transaction> iterator = transactions.iterator();

        while (iterator.hasNext()) {
            Transaction transaction = iterator.next();

            if (transaction.getId().equals(id)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public List<Transaction> findByCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }
        return transactions.stream()
                .filter(t -> t.getCategory() == category)
                .toList();
    }

    public List<Transaction> findByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must not be after to date");
        }

        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(from) && !t.getDate().isAfter(to))
                .toList();
    }

    public List<Transaction> sortByDate() {
       return transactions.stream()
               .sorted(Comparator.comparing(Transaction::getDate)
                       .thenComparing(Transaction::getId))
               .toList();
    }

    public Map<Category, BigDecimal> expensesByCategory() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toMap(
                        Transaction::getCategory,
                        Transaction::getAmount,
                        BigDecimal::add,
                        LinkedHashMap::new
                ));
    }

    public List<Transaction> findLargestExpenses(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must not be negative");
        }

        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed()
                        .thenComparing(Transaction::getDate)
                        .thenComparing(Transaction::getId))
                .limit(limit)
                .toList();
    }

    public Optional<YearMonth> findMonthWithLargestExpenses() {
        Map<YearMonth, BigDecimal> expensesByMonth = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getDate()),
                        Collectors.reducing(
                                new BigDecimal("0.00"),
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
        return expensesByMonth.entrySet().stream()
                .max(
                        Map.Entry.<YearMonth, BigDecimal>comparingByValue()
                        .thenComparing(
                                Map.Entry::getKey,
                                Comparator.reverseOrder()
                        ))
                .map(Map.Entry::getKey);
    }

    public Optional<Category> findMostExpensiveCategory() {
        Map<Category, BigDecimal> expenses = expensesByCategory();
        return expenses.entrySet().stream()
                .max (
                        Map.Entry.<Category, BigDecimal>comparingByValue()
                                .thenComparing(
                                        entry -> entry.getKey().name(),
                                        Comparator.reverseOrder()
                                )
                )
                .map(Map.Entry::getKey);
    }

    public Optional<BigDecimal> calculateAverageExpense() {
        BigDecimal expensesSum = transactions.stream()
                .filter(IS_EXPENSE)
                .map(Transaction::getAmount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        long expensesCount = transactions.stream()
                .filter(IS_EXPENSE)
                .count();
        if (expensesCount == 0) {
            return Optional.empty();
        }
        return Optional.of(expensesSum.divide(new BigDecimal(expensesCount),2, RoundingMode.HALF_UP));
    }
}
