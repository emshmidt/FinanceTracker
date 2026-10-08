package com.learning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CsvTransactionExporterTest {
    CsvTransactionExporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        exporter = new CsvTransactionExporter();
    }

    @Test
    void shouldExportHeaderForEmptyList() throws IOException {
        Path file = tempDir.resolve("transations.csv");
        String expected = "id,title,amount,type,date,category";

        exporter.export(List.of(), file);

        assertEquals(expected, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void shouldExportTransactionsInProvidedOrder() throws IOException {
        Transaction first = new Transaction(
                "Продукты", new BigDecimal("100.00"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        Transaction second = new Transaction(
                "Зарплата", new BigDecimal("500.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        Path file = tempDir.resolve("transactions.csv");

        exporter.export(List.of(first, second), file);

        String actual = Files.readString(file, StandardCharsets.UTF_8);

        String expected =
                "id,title,amount,type,date,category"
                        + System.lineSeparator()
                        + "\"" + first.getId() + "\","
                        + "\"Продукты\",\"100.00\",\"EXPENSE\",\"2025-01-15\",\"FOOD\""
                        + System.lineSeparator()
                        + "\"" + second.getId() + "\","
                        + "\"Зарплата\",\"500.00\",\"INCOME\",\"2025-01-15\",\"SALARY\"";

        assertEquals(expected, actual);
    }

    @Test
    void shouldCreateMissingParentDirectories() throws IOException {
        Path file = tempDir.resolve("nested/data/transactions.csv");

        assertFalse(Files.exists(file.getParent()));

        exporter.export(List.of(), file);

        assertTrue(Files.isDirectory(file.getParent()));
        assertEquals(
                "id,title,amount,type,date,category",
                Files.readString(file, StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldOverwriteExistingFile() throws IOException {
        Transaction first = new Transaction(
                "Продукты", new BigDecimal("100.00"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        Transaction second = new Transaction(
                "Зарплата", new BigDecimal("500.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        Path file = tempDir.resolve("transactions.csv");

        exporter.export(List.of(first, second), file);
        exporter.export(List.of(second), file);

        String actual = Files.readString(file, StandardCharsets.UTF_8);

        String expected =
                "id,title,amount,type,date,category"
                        + System.lineSeparator()
                        + "\"" + second.getId() + "\","
                        + "\"Зарплата\",\"500.00\",\"INCOME\",\"2025-01-15\",\"SALARY\"";

        assertEquals(expected, actual);
    }

    @Test
    void shouldWriteCyrillicUsingUtf8() throws IOException{
        Transaction transaction = new Transaction(
                "Зарплата в январе", new BigDecimal("500.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        Path file = tempDir.resolve("transactions.csv");

        exporter.export(List.of(transaction), file);

        String actual = Files.readString(file, StandardCharsets.UTF_8);

        String expected =
                "id,title,amount,type,date,category"
                        + System.lineSeparator()
                        + "\"" + transaction.getId() + "\","
                        + "\"Зарплата в январе\",\"500.00\",\"INCOME\",\"2025-01-15\",\"SALARY\"";

        assertEquals(expected, actual);
    }

    @Test
    void shouldRejectNullTransactions() {
        Path file = tempDir.resolve("transactions.csv");

        assertThrows(
                IllegalArgumentException.class,
                () -> exporter.export(null, file)
        );
    }

    @Test
    void shouldRejectNullPath() {
        Transaction transaction = new Transaction(
                "Зарплата", new BigDecimal("500.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> exporter.export(List.of(transaction), null)

        );
    }

    @Test
    void shouldRejectNullElementWithoutChangingExistingFile() throws IOException{
        Transaction transaction = new Transaction(
                "Зарплата", new BigDecimal("500.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        Path file = tempDir.resolve("transactions.csv");

        exporter.export(List.of(transaction), file);
        List<Transaction> invalidTransactions =
                Arrays.asList(transaction, null);

        assertThrows(
                IllegalArgumentException.class,
                () -> exporter.export(invalidTransactions, file)
        );

        String actual = Files.readString(file, StandardCharsets.UTF_8);

        String expected =
                "id,title,amount,type,date,category"
                        + System.lineSeparator()
                        + "\"" + transaction.getId() + "\","
                        + "\"Зарплата\",\"500.00\",\"INCOME\",\"2025-01-15\",\"SALARY\"";

        assertEquals(expected, actual);
    }

    @Test
    void shouldPropagateIOExceptionWhenPathIsDirectory() {
        Transaction transaction = new Transaction(
                "Зарплата", new BigDecimal("500.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        assertThrows(
                IOException.class,
                () -> exporter.export(List.of(transaction), tempDir)
        );
    }

    @Test
    void shouldExportServiceTransactionsWithoutChangingServiceState() throws IOException{
        InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
        TransactionService service = new TransactionService(repository);

        Transaction first = new Transaction(
                "Продукты", new BigDecimal("100.00"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        Transaction second = new Transaction(
                "Зарплата", new BigDecimal("500.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        service.add(first);
        service.add(second);

        List<Transaction> allTransactions = service.findAll();
        BigDecimal balance = service.calculateBalance();

        Path file = tempDir.resolve("transactions.csv");
        exporter.export(service.findAll(), file);

        List<Transaction> allTransactions2 = service.findAll();
        BigDecimal balance2 = service.calculateBalance();

        String actual = Files.readString(file, StandardCharsets.UTF_8);

        String expected =
                "id,title,amount,type,date,category"
                        + System.lineSeparator()
                        + "\"" + first.getId() + "\","
                        + "\"Продукты\",\"100.00\",\"EXPENSE\",\"2025-01-15\",\"FOOD\""
                        + System.lineSeparator()
                        + "\"" + second.getId() + "\","
                        + "\"Зарплата\",\"500.00\",\"INCOME\",\"2025-01-15\",\"SALARY\"";

        assertEquals(expected, actual);

        assertEquals(allTransactions, allTransactions2);
        assertEquals(balance, balance2);
    }
}
