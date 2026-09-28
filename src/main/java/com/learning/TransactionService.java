package com.learning;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private final List<Transaction> transactions = new ArrayList<>();

    public void add(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction can't be null");
        }
        transactions.add(transaction);
    }

    public List<Transaction> findAll() {
        return List.copyOf(transactions);
    }

    public BigDecimal calculateBalance() {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction transaction: transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                balance = balance.add(transaction.getAmount());
            } else {
                balance = balance.subtract(transaction.getAmount());
            }
        }
        return balance;
    }
}
