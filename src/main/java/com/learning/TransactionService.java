package com.learning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TransactionService {
    public TransactionService(TransactionRepository repository) {
        if (repository == null) {
            throw  new IllegalArgumentException("Repository must not be null");
        }
        this.repository = repository;
    }

    public TransactionService() {
        this(new InMemoryTransactionRepository());
    }
    private final TransactionRepository repository;

    private static final Predicate<Transaction> IS_EXPENSE =
            t -> t.getType() == TransactionType.EXPENSE;

    public void add(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction must not be null");
        }

        if (findById(transaction.getId()).isPresent()) {
            throw new IllegalArgumentException("Transaction with this ID already exists");
        }

        repository.save(transaction);
    }

    public List<Transaction> findAll() {
        return repository.findAll();
    }

    public BigDecimal calculateBalance() {
        BigDecimal balance = new BigDecimal("0.00");
        List<Transaction> transactions = repository.findAll();

        for (Transaction transaction : transactions) {
            if (!IS_EXPENSE.test(transaction)) {
                balance = balance.add(transaction.getAmount());
            } else {
                balance = balance.subtract(transaction.getAmount());
            }
        }
        return balance;
    }

    public Optional<Transaction> findById(UUID id) { return repository.findById(id); }

    public Transaction getByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(
                () -> new TransactionNotFoundException("Transaction not found: " + id)
        );
    }

    public boolean removeById(UUID id) {
        if (repository.findById(id).isPresent()) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Transaction> findByCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }
        return repository.findAll().stream()
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

        return repository.findAll().stream()
                .filter(t -> !t.getDate().isBefore(from) && !t.getDate().isAfter(to))
                .toList();
    }

    public List<Transaction> sortByDate() {
       return repository.findAll().stream()
               .sorted(Comparator.comparing(Transaction::getDate)
                       .thenComparing(Transaction::getId))
               .toList();
    }

    public Map<Category, BigDecimal> expensesByCategory() {
        return repository.findAll().stream()
                .filter(IS_EXPENSE)
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

        return repository.findAll().stream()
                .filter(IS_EXPENSE)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed()
                        .thenComparing(Transaction::getDate)
                        .thenComparing(Transaction::getId))
                .limit(limit)
                .toList();
    }

    public Optional<YearMonth> findMonthWithLargestExpenses() {
        Map<YearMonth, BigDecimal> expensesByMonth = repository.findAll().stream()
                .filter(IS_EXPENSE)
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
        int count = 0;
        BigDecimal sum = new BigDecimal("0.00");

        for (Transaction transaction : repository.findAll()) {
            if (IS_EXPENSE.test(transaction)) {
                sum = sum.add(transaction.getAmount());
                count ++;
            }
        }
        if(count == 0) {
            return Optional.empty();
        }
        return Optional.of(
                sum.divide(new BigDecimal(count),2, RoundingMode.HALF_UP)
        );
    }
}
