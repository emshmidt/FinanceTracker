package com.learning;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class TransactionService {
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
        for (Transaction transaction: transactions) {
            if (transaction.getId().equals(id)) {
                return Optional.of(transaction);
            }
        }
        return Optional.empty();
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
        List<Transaction> result = new ArrayList<>();

        for (Transaction transaction: transactions) {
            if (transaction.getCategory() == category) {
                result.add(transaction);
            }
        }

        return List.copyOf(result);
    }

    public List<Transaction> findByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Dates must not be null");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must not be after to date");
        }

        List<Transaction> result = new ArrayList<>();

        for (Transaction transaction: transactions) {
            if (!transaction.getDate().isBefore(from)
                    && !transaction.getDate().isAfter(to)) {
                result.add(transaction);
            }
        }

        return List.copyOf(result);
    }

    public List<Transaction> sortByDate() {
        List<Transaction> result = new ArrayList<>(transactions);

       result.sort(Comparator.comparing(Transaction::getDate)
                .thenComparing(Transaction::getId));

       return List.copyOf(result);
    }

    public Map<Category, BigDecimal> expensesByCategory() {
        Map<Category, BigDecimal> result = new LinkedHashMap<>();

        for (Transaction transaction:transactions) {
            if (transaction.getType() == TransactionType.EXPENSE) {
                result.put(transaction.getCategory(),
                        result.getOrDefault(transaction.getCategory(), BigDecimal.ZERO).add(transaction.getAmount())
                );
            }
        }

        return result;
    }
}
