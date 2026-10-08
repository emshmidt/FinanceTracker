package com.learning;

import java.util.*;

public class InMemoryTransactionRepository implements TransactionRepository{
    private final Map<UUID, Transaction> storage =
            new LinkedHashMap<>();

    @Override
    public void save(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction must not be null");
        }
        if (storage.containsKey(transaction.getId())) {
            throw  new InvalidTransactionException("Transaction with this id already exists");
        }
        storage.put(transaction.getId(), transaction);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
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
            throw new IllegalArgumentException("Id must not be null");
        }
        storage.remove(id);
    }

}
