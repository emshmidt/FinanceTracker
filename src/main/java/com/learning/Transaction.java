package com.learning;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class Transaction {
    private final UUID id;
    private final String title;
    private final BigDecimal amount;
    private final TransactionType type;
    private final LocalDate date;
    private final Category category;


    public Transaction(String title, BigDecimal amount, TransactionType type, LocalDate date, Category category) {
        if (title == null || title.isBlank()){
            throw new InvalidTransactionException("Title must not be null or blank");
        }
        if (amount == null) {
            throw new InvalidTransactionException("Amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be positive");
        }
        if (amount.scale() > 2) {
            throw  new InvalidTransactionException("Amount must have at most 2 decimal places");
        }
        if (type == null) {
            throw new InvalidTransactionException("Transaction type must not be null");
        }
        if (date == null) {
            throw new InvalidTransactionException("Date must not be null");
        }
        if (category == null) {
            throw new InvalidTransactionException("Category must not be null");
        }

        this.id = UUID.randomUUID();
        this.title = title;
        this.amount = amount.setScale(2);
        this.type = type;
        this.date = date;
        this.category = category;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public Category getCategory() {
        return category;
    }
}
