package com.learning;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CsvTransactionReader {

    public List<Transaction> read(Path path) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        List<Transaction> transactions = new ArrayList<>();
        Set<UUID> ids = new HashSet<>();

        try (
                Reader reader = Files.newBufferedReader(path);
                CSVParser parser = format.parse(reader)
        ) {
            validateHeader(parser);

            for (CSVRecord record : parser) {
                if (record.size() != 6) {
                    throw new IllegalArgumentException(
                            "Record " + record.getRecordNumber()
                                    + ": expected 6 fields, got " + record.size());
                }

                Transaction transaction;

                try {
                    transaction = Transaction.restore(
                            parseId(record),
                            getRequiredField(record, "title"),
                            parseAmount(record),
                            parseType(record),
                            parseDate(record),
                            parseCategory(record)
                    );
                } catch (InvalidTransactionException e) {
                    throw new IllegalArgumentException(
                            "Record " + record.getRecordNumber()
                                    + ": invalid transaction: " + e.getMessage(),
                            e
                    );
                }

                if (!ids.add(transaction.getId())) {
                    throw new IllegalArgumentException(
                            "Record " + record.getRecordNumber()
                                    + ": duplicate UUID in field id: "
                                    + transaction.getId());
                }

                transactions.add(transaction);
            }

        }

        return List.copyOf(transactions);
    }

    private void validateHeader(CSVParser parser) {
        List<String> expected = List.of(
                "id", "title", "amount", "type", "date", "category"
        );
        List<String> actual = parser.getHeaderNames();

        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(
                    "Wrong header CSV. Expected: " + expected + "actual: " + actual
            );
        }
    }

    private String getRequiredField(CSVRecord record, String name) {
        if (!record.isMapped(name)) {
            throw new IllegalArgumentException("Record: " +record.getRecordNumber() + "Column " + name + " must be present");
        }

        if (!record.isSet(name)) {
            throw new IllegalArgumentException("Record: " +record.getRecordNumber() + "Value of column " + name + " must be set");
        }

        if (record.get(name) == null || record.get(name).isBlank()) {
            throw new IllegalArgumentException("Record: " +record.getRecordNumber() + "Field " + name + " must not be null");
        }

        return record.get(name);
    }

    private UUID parseId(CSVRecord record) {
        String value = getRequiredField(record, "id");

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Record:" + record.getRecordNumber() + " invalid id: "
                    + value, e);
        }
    }

    private BigDecimal parseAmount(CSVRecord record) {
        String value = getRequiredField(record, "amount");

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Record:" + record.getRecordNumber() + " invalid amount: "
                            + value, e);
        }
    }

    private TransactionType parseType(CSVRecord record) {
        String value = getRequiredField(record, "type");

        try {
            return TransactionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Record:" + record.getRecordNumber() + " invalid type: "
                            + value, e);
        }
    }

    private LocalDate parseDate(CSVRecord record) {
        String value = getRequiredField(record, "date");

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Record:" + record.getRecordNumber() + " invalid date: "
                            +value, e);
        }
    }

    private Category parseCategory(CSVRecord record) {
        String value = getRequiredField(record, "category");

        try {
            return Category.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Record:" + record.getRecordNumber() + " invalid category: "
                            + value, e);
        }
    }
}
