package com.learning;

import java.time.format.DateTimeFormatter;

public class ConsoleTransactionFormatter implements TransactionFormatter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public String format(Transaction transaction) {
        return transaction.getDate().format(DATE_FORMAT)
                + " | " + transaction.getCategory()
                + " | " + transaction.getTitle()
                + " | " + transaction.getAmount().toPlainString();
    }
}