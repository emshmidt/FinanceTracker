package com.learning;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvTransactionExporter {
    public void export(List<Transaction> transactions, Path path)
            throws IOException {
        if (transactions == null) {
            throw new IllegalArgumentException("Transactions must not be null");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }
        for (Transaction transaction: transactions) {
            if (transaction == null) {
                throw new IllegalArgumentException("Transaction must not be null");
            }
        }

        CsvTransactionFormatter formatter = new CsvTransactionFormatter();

        Path parent = path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer =
                     Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("id,title,amount,type,date,category");

            for (Transaction transaction : transactions) {
                writer.newLine();
                writer.write(formatter.format(transaction));
            }
        }

    }
}
