package com.learning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class FileTransactionRepositoryTest {
    private FileTransactionRepository repository;
    private Path file;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        file = tempDir.resolve("transactions.csv");
        repository = new FileTransactionRepository(file);
    }

    @Test
    void shouldStartEmptyWhenFileDoesNotExist() {
        assertTrue(repository.findAll().isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void shouldRejectNullPath() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new FileTransactionRepository(null)
        );
    }

    @Test
    void shouldLoadEmptyRepositoryFromHeaderOnlyFile() throws IOException{
        Files.writeString(file, "id,title,amount,type,date,category");
        FileTransactionRepository loadedRepository = new FileTransactionRepository(file);

        assertTrue(loadedRepository.findAll().isEmpty());
    }

    @Test
    void shouldRejectEmptyFile() throws IOException{
        Files.writeString(file, "");

        assertThrows(
                IllegalArgumentException. class,
                () -> new FileTransactionRepository(file)
        );
    }

    @Test
    void shouldWrapIOExceptionWhenPathIsDirectory() {
        assertThrows(
                UncheckedIOException.class,
                () -> new FileTransactionRepository(tempDir)
        );
    }

    @Test
    void shouldPersistTransactionAndRestoreAllFields() {
        CsvTransactionExporter exporter = new CsvTransactionExporter();

        Transaction transaction = new Transaction(
                "Продукты", new BigDecimal("100.00"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        repository.save(transaction);

        FileTransactionRepository loadedRepository =
                new FileTransactionRepository(file);

        Transaction restored = loadedRepository
                .findById(transaction.getId())
                .orElseThrow();

        assertEquals(transaction.getId(), restored.getId());
        assertEquals(transaction.getTitle(), restored.getTitle());
        assertEquals(transaction.getAmount(), restored.getAmount());
        assertEquals(transaction.getType(), restored.getType());
        assertEquals(transaction.getDate(), restored.getDate());
        assertEquals(transaction.getCategory(), restored.getCategory());
    }

    @Test
    void shouldPreserveInsertionOrderAfterReload() {
        Transaction first = new Transaction(
                "Зарплата", new BigDecimal("100.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction second = new Transaction(
                "Продукты", new BigDecimal("60.00"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        Transaction third = new Transaction(
                "Возврат за продукты", new BigDecimal("30.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        repository.save(first);
        repository.save(second);
        repository.save(third);
        List<Transaction> actual = repository.findAll();

        FileTransactionRepository loadedRepository =
                new FileTransactionRepository(file);


        List<UUID> restoredIds = loadedRepository.findAll().stream()
                .map(Transaction::getId)
                .toList();

        assertEquals(
                List.of(first.getId(), second.getId(), third.getId()),
                restoredIds
        );
    }

    @Test
    void shouldRestoreTitleWithCommaQuotesAndLineBreak() {
        Transaction transaction = new Transaction(
                "Зарплата, премия \n \"Июнь\"", new BigDecimal("100.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        repository.save(transaction);

        FileTransactionRepository loadedRepository =
                new FileTransactionRepository(file);


        Transaction restored = loadedRepository
                .findById(transaction.getId())
                .orElseThrow();

        assertEquals(restored.getTitle(), transaction.getTitle());
    }

    @Test
    void shouldRejectDuplicateIdWithoutChangingMemoryOrFile() throws IOException{
        Transaction transaction = new Transaction(
                "Зарплата, премия \n \"Июнь\"", new BigDecimal("100.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        repository.save(transaction);
        byte[] before = Files.readAllBytes(file);

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(transaction)
        );

        assertEquals(List.of(transaction), repository.findAll());

        FileTransactionRepository loadedRepository =
                new FileTransactionRepository(file);


        Transaction restored = loadedRepository
                .findById(transaction.getId())
                .orElseThrow();

        assertEquals(restored.getTitle(), transaction.getTitle());

        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void shouldPersistDeletionWithoutRemovingOtherTransactions() {
        Transaction first = new Transaction(
                "Зарплата", new BigDecimal("100.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction second = new Transaction(
                "Продукты", new BigDecimal("60.00"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        Transaction third = new Transaction(
                "Возврат за продукты", new BigDecimal("30.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        repository.save(first);
        repository.save(second);
        repository.save(third);

        repository.deleteById(second.getId());
        List<Transaction> actual = repository.findAll();

        FileTransactionRepository loadedRepository =
                new FileTransactionRepository(file);

        List<Transaction> restored = loadedRepository
                .findAll();

        assertEquals(restored.get(0).getId(), actual.get(0).getId());
        assertEquals(restored.get(1).getId(), actual.get(1).getId());
        assertEquals(List.of(first, third), actual);

        List<UUID> restoredIds = restored.stream()
                .map(Transaction::getId)
                .toList();

        assertEquals(
                List.of(first.getId(), third.getId()),
                restoredIds
        );
    }

    @Test
    void shouldPersistEmptyRepositoryAfterDeletingLastTransaction() {
        Transaction transaction = new Transaction(
                "Зарплата", new BigDecimal("100.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        repository.save(transaction);
        repository.deleteById(transaction.getId());
        List<Transaction> actual = repository.findAll();

        FileTransactionRepository loadedRepository =
                new FileTransactionRepository(file);

        List<Transaction> restored = loadedRepository
                .findAll();

        assertEquals(List.of(), actual);
        assertEquals(List.of(), restored);
    }

    @Test
    void shouldDoNothingWhenDeletingUnknownId() {
        Transaction transaction = new Transaction(
                "Зарплата", new BigDecimal("100.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );

        repository.save(transaction);
        repository.deleteById(UUID.randomUUID());
        List<Transaction> actual = repository.findAll();

        FileTransactionRepository loadedRepository =
                new FileTransactionRepository(file);

        Transaction restored = loadedRepository
                .findById(transaction.getId())
                        .orElseThrow();

        assertEquals(List.of(transaction), actual);
        assertEquals(transaction.getTitle(), restored.getTitle());
    }

    @Test
    void shouldDoNothingWhenDeletingTransactionTwice() {
        Transaction first = new Transaction(
                "Зарплата", new BigDecimal("100.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction second = new Transaction(
                "Продукты", new BigDecimal("60.00"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        Transaction third = new Transaction(
                "Возврат за продукты", new BigDecimal("30.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        repository.save(first);
        repository.save(second);
        repository.save(third);

        repository.deleteById(second.getId());

        repository.deleteById(second.getId());
        List<Transaction> actual = repository.findAll();

        FileTransactionRepository loadedRepository =
                new FileTransactionRepository(file);

        List<Transaction> restored = loadedRepository
                .findAll();

        assertEquals(restored.get(0).getId(), actual.get(0).getId());
        assertEquals(restored.get(1).getId(), actual.get(1).getId());
        assertEquals(List.of(first, third), actual);

        List<UUID> restoredIds = restored.stream()
                .map(Transaction::getId)
                .toList();

        assertEquals(
                List.of(first.getId(), third.getId()),
                restoredIds
        );
    }

    @Test
    void shouldRejectNullTransaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );
    }

    @Test
    void shouldRejectNullIdWhenFindingTransaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findById(null)
        );
    }

    @Test
    void shouldRejectNullIdWhenDeletingTransaction() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.deleteById(null)
        );
    }

    @Test
    void shouldReturnUnmodifiableSnapshot() {
        List<Transaction> found = repository.findAll();

        assertThrows(
                UnsupportedOperationException.class,
                found::clear
        );
    }

    @Test
    void shouldPreserveSnapshotAfterSaveAndDelete() {
        Transaction first = new Transaction(
                "Зарплата", new BigDecimal("100.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.SALARY
        );
        Transaction second = new Transaction(
                "Продукты", new BigDecimal("60.00"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );
        Transaction third = new Transaction(
                "Возврат за продукты", new BigDecimal("30.00"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 15), Category.FOOD
        );

        repository.save(first);
        repository.save(second);
        List<Transaction> found = repository.findAll();

        repository.deleteById(second.getId());
        repository.save(third);

        assertEquals(List.of(first, second), found);
    }

    @Test
    void shouldRejectInvalidHeader() throws IOException{
        Files.writeString(file, "wrong, header");

        assertThrows(
                IllegalArgumentException.class,
                () -> new FileTransactionRepository(file)
        );
    }

    @Test
    void shouldRejectDuplicateIdsInFile() throws IOException {
        Transaction transaction = new Transaction(
                "Продукты",
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 8),
                Category.FOOD
        );

        new CsvTransactionExporter().export(
                List.of(transaction, transaction),
                file
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new FileTransactionRepository(file)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void shouldRejectInvalidTransactionField(
            int index,
            String invalidValue,
            String fieldName
    ) throws IOException{
        String[] fields = {
                "11111111-1111-1111-1111-111111111111",
                "Продукты",
                "150.50",
                "EXPENSE",
                "2026-10-07",
                "FOOD"
        };

        fields[index] = invalidValue;

        Files.writeString(
                file, "id,title,amount,type,date,category\n"
                        + String.join(",",fields));


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new FileTransactionRepository(file)
        );

        assertTrue(exception.getMessage().contains(fieldName));
        assertTrue(exception.getMessage().contains("1"));
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of(0, "not-a-uuid", "id"),
                Arguments.of(1, "", "title"),
                Arguments.of(2, "abc", "amount"),
                Arguments.of(3, "UNKNOWN", "type"),
                Arguments.of(4, "2026-99-07", "date"),
                Arguments.of(5, "UNKNOWN", "category")
        );
    }

    @Test
    void shouldPreserveMemoryWhenSaveFails() throws IOException {
        Transaction first = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 8), Category.FOOD
        );

        Transaction second = new Transaction(
                "Зарплата", new BigDecimal("500.00"),
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 8), Category.SALARY
        );

        repository.save(first);

        Files.delete(file);
        Files.createDirectory(file);
        Files.writeString(file.resolve("marker.txt"), "busy");

        assertThrows(
                UncheckedIOException.class,
                () -> repository.save(second)
        );

        assertEquals(List.of(first), repository.findAll());
        assertEquals(Optional.empty(), repository.findById(second.getId()));
    }

    @Test
    void shouldPreserveMemoryWhenDeleteFails() throws IOException{
        Transaction first = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 8), Category.FOOD
        );

        Transaction second = new Transaction(
                "Зарплата", new BigDecimal("500.00"),
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 8), Category.SALARY
        );

        repository.save(first);
        repository.save(second);

        Files.delete(file);
        Files.createDirectory(file);
        Files.writeString(file.resolve("marker.txt"), "busy");

        assertThrows(
                UncheckedIOException.class,
                () -> repository.deleteById(second.getId())
        );

        assertEquals(List.of(first, second), repository.findAll());
    }

    @Test
    void shouldRemoveTemporaryFileWhenPersistenceFails() throws IOException{
        Transaction first = new Transaction(
                "Продукты", new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 8), Category.FOOD
        );

        Transaction second = new Transaction(
                "Зарплата", new BigDecimal("500.00"),
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 8), Category.SALARY
        );

        repository.save(first);

        Files.delete(file);
        Files.createDirectory(file);
        Files.writeString(file.resolve("marker.txt"), "busy");

        assertThrows(
                UncheckedIOException.class,
                () -> repository.save(second)
        );

        try (Stream<Path> files = Files.list(tempDir)) {
            assertFalse(
                    files.anyMatch(path ->
                            path.getFileName().toString()
                                    .startsWith("transactions-")
                                    && path.getFileName().toString()
                                    .endsWith(".tmp"))
            );
        }
    }
}
