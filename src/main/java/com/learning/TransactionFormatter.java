package com.learning;

@FunctionalInterface
public interface TransactionFormatter {
    String format(Transaction transaction);
}
