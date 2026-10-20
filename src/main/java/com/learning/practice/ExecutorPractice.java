package com.learning.practice;

import com.learning.Category;
import com.learning.Transaction;
import com.learning.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

public class ExecutorPractice {
    public static void main(String[] args) throws Exception{

        executorTask();
    }

    static void executorTask() {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable task = () -> {
            System.out.println("Work begin " + Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
                System.out.println("Success. Task finished");
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception: " + e.getMessage());

                Thread.currentThread().interrupt();
                return;
            }
        };

        try {
            executor.submit(task);
            executor.shutdown();
            System.out.println("Shutdown");
            boolean waitTerm = executor.awaitTermination(50, TimeUnit.MILLISECONDS);
            System.out.println("first wait: " + waitTerm);
            if (!waitTerm) {
                executor.shutdownNow();
            }
            boolean secondWait = executor.awaitTermination(2, TimeUnit.SECONDS);
            System.out.println("Second wait: " + secondWait);

        } catch (InterruptedException e) {
            executor.shutdownNow();

            Thread.currentThread().interrupt();
            return;
        }
        finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }



    }

    static void waitingTask() throws Exception{
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Runnable task = () -> {
                System.out.println("Work begin" + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                    System.out.println("Success. Task finished");
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception: " + e.getMessage());

                    Thread.currentThread().interrupt();
                    return;
                }

            };

            Future<?> future = executor.submit(task);
            try {
                future.get(50, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("Time out exception " + e.getMessage());
                boolean cancellation = future.cancel(true);
                System.out.println("Cancellation: " + cancellation);
                System.out.println("Future is cancelled: " + future.isCancelled());
                if (future.isCancelled()) {
                    try {
                        future.get();
                    } catch (CancellationException c) {
                        System.out.println("Cancellation Exception " + c.getMessage());
                    }
                }
            }

        }
    }

    static void errorInTask() throws ExecutionException {
        try (ExecutorService executor = Executors.newFixedThreadPool(2)){
            Callable<Integer> task = () -> {
                return Integer.valueOf("abc");
            };

            Future<Integer> futureint = executor.submit(task);

            try {
                Integer integer = futureint.get();
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception " + e.getCause());
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException e) {
                System.out.println("Execution exception " + e.getCause());
                return;
            }

        }
    }

    static BigDecimal calculateIncome(List<Transaction> transactions) {
        System.out.println(
                "Расчёт доходов: " + Thread.currentThread().getName()
        );
        return transactions.stream().filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    static BigDecimal calculateExpenses(List<Transaction> transactions) {
        System.out.println(
                "Расчёт расходов: " + Thread.currentThread().getName()
        );
        return transactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
    }

    static void calculateBalance() throws Exception{
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

        Callable<BigDecimal> incomeTask = () -> calculateIncome(snapshot);

        Callable<BigDecimal> expensesTask = () -> calculateExpenses(snapshot);

        try (ExecutorService executors = Executors.newFixedThreadPool(2)) {
            Future<BigDecimal> incomeFuture = executors.submit(incomeTask);
            Future<BigDecimal> expenseFuture = executors.submit(expensesTask);

            BigDecimal income = incomeFuture.get();
            System.out.println("Доходы: " + income);
            BigDecimal expense = expenseFuture.get();
            System.out.println("Расходы: " + expense);

            System.out.println(
                    "Расчёт баланса: " + Thread.currentThread().getName()
            );
            BigDecimal balance = income.subtract(expense);
            System.out.println("Баланс: " + balance);
        }
    }

}
