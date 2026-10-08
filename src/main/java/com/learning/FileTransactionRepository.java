package com.learning;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FileTransactionRepository implements TransactionRepository{
    private final Path path;

    private final CsvTransactionReader reader =
            new CsvTransactionReader();

    private final CsvTransactionExporter exporter =
            new CsvTransactionExporter();

    private final Map<UUID, Transaction> storage =
            new LinkedHashMap<>();

    public FileTransactionRepository(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }

        this.path = path;

        load(path);
    }

    @Override
    public void save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException(
                    "Transaction must not be null"
            );
        }

        if (storage.containsKey(transaction.getId())) {
            throw new IllegalArgumentException(
                    "Transaction's id must be unique"
            );
        }

        Map<UUID, Transaction> nextState =
                new LinkedHashMap<>(storage);
        nextState.put(transaction.getId(), transaction);
        persist(nextState);

        storage.clear();
        storage.putAll(nextState);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Id must not be null"
            );
        }

        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Transaction> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public void deleteById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Id must not be null"
            );
        }
        if (!storage.containsKey(id)) {
            return;
        }

        Map<UUID, Transaction> nextState = new LinkedHashMap<>(storage);
        nextState.remove(id);
        persist(nextState);

        storage.clear();
        storage.putAll(nextState);
    }

    private void load(Path path) {
        try {
            List<Transaction> transactions = reader.read(path);

            for (Transaction transaction: transactions) {
                storage.put(transaction.getId(), transaction);
            }
        } catch (NoSuchFileException e) {
            //Файла нет - хранилище остается пустым
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Can not load transactions from " + path, e);
        }
    }

    private void persist(Map<UUID, Transaction> nextState) {
        Path target = path.toAbsolutePath();
        Path directory = target.getParent();
        Path tempFile = null;

        try {
            Files.createDirectories(directory);

            tempFile =
                    Files.createTempFile(directory, "transactions-", ".tmp");

            exporter.export(
                    List.copyOf(nextState.values()),
                    tempFile
            );
            Files.move(
                    tempFile,
                    target,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (IOException e) {

            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                 } catch (IOException cleanupError) {
                    e.addSuppressed(cleanupError);
                 }
        }

        throw new UncheckedIOException(
                "Cannot persist transactions to " + target, e);
        }


    }
}
