package com.learning;

public class CsvTransactionFormatter implements TransactionFormatter{

    @Override
    public String format(Transaction transaction) {
        String transactionId = "\"" + transaction.getId().toString() + "\"";
        String transactionTitle =
                "\"" + transaction.getTitle().replace("\"", "\"\"") + "\"";
        String transactionAmount = "\"" + transaction.getAmount().toPlainString() + "\"";
        String transactionType = "\"" + transaction.getType().name() + "\"";
        String transactionDate = "\"" + transaction.getDate().toString() + "\"";
        String transactionCategory = "\"" + transaction.getCategory().name() + "\"";

        return String.join(",", transactionId,
                transactionTitle, transactionAmount,
                transactionType, transactionDate, transactionCategory);
    }
}
