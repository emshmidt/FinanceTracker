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
        if (title == null || title.stripLeading().isEmpty()){
            throw new IllegalArgumentException("Title must not be null or blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must not be null or not positive");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type must not be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }

        this.id = UUID.randomUUID();
        this.title = title;
        this.amount = amount;
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
