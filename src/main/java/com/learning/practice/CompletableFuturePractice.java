package com.learning.practice;

import com.learning.Category;
import com.learning.Transaction;
import com.learning.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

import static com.learning.practice.ExecutorPractice.calculateExpenses;
import static com.learning.practice.ExecutorPractice.calculateIncome;

public class CompletableFuturePractice {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Transaction salary = new Transaction(
                "Salary", new BigDecimal("1000"), TransactionType.INCOME,
                LocalDate.of(2025, 1, 1), Category.SALARY
        );

        Transaction produce = new Transaction(
                "Produce", new BigDecimal("150.50"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1), Category.FOOD
        );

        Transaction ride = new Transaction(
                "Train ride", new BigDecimal("49.50"), TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1), Category.TRANSPORT
        );

        List<Transaction> snapshot = List.of(salary, produce, ride);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)){

            CompletableFuture<BigDecimal> expensesFuture = CompletableFuture.supplyAsync(
                    () -> calculateExpenses(snapshot), executor
            );

            CompletableFuture<String> textExpenses = expensesFuture.thenApply(
                    value -> {
                        System.out.println(Thread.currentThread().getName());
                       return "Расходы: " + value;
                    }
            );


            CompletableFuture<BigDecimal> incomeFuture = CompletableFuture.supplyAsync(
                    () -> calculateIncome(snapshot), executor
            );

            CompletableFuture<BigDecimal> balance = incomeFuture.thenCombine(
                    expensesFuture, BigDecimal::subtract
                    );

            CompletableFuture<List<Transaction>> snapshotFuture =
                    CompletableFuture.completedFuture(snapshot);

            CompletableFuture<BigDecimal> expenses2 = snapshotFuture.thenCompose(
                    (value) ->
                        CompletableFuture.supplyAsync(
                                () -> calculateExpenses(value), executor
                        )
            );


            CompletableFuture<Void> messageFuture = CompletableFuture.runAsync(
                    () -> System.out.println("Подготовка отчета: " + Thread.currentThread().getName()),
                    executor
            );

            CompletableFuture<Void> all = CompletableFuture.allOf(textExpenses, balance, expenses2, messageFuture);

            all.join();
            System.out.println(textExpenses.get());
            System.out.println(balance.get());
            System.out.println(expenses2.get());


            CompletableFuture<String> failedReport =
                    CompletableFuture.supplyAsync(() -> {
                        throw new IllegalStateException("Ошибка подготовки отчёта");
                    }, executor);

            CompletableFuture<String> recoveredReport = failedReport.whenComplete((string, error) -> {
                if (error != null) {
                    System.out.println(error);
                } else {
                    System.out.println(string);
                }
            }).exceptionally(error -> "Отчет не доступен");

            String result = recoveredReport.join();
            System.out.println(result);



        }



    }
}
