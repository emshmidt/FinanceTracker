package com.learning;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FunctionalPractice {
    public static void main(String[] args) {
        Transaction transaction = new Transaction(
                "Продукты",
                new BigDecimal("150.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                Category.FOOD
        );

        Predicate<Transaction> isExpense =
                t -> t.getType() == TransactionType.EXPENSE;

        Function<Transaction, BigDecimal> getAmount =
                Transaction::getAmount;

        Consumer<Transaction> printTitle =
                t -> System.out.println(t.getTitle());

        Supplier<List<Transaction>> createList =
                () -> new ArrayList<>();

        BinaryOperator<BigDecimal> addAmounts =
                BigDecimal::add;

        Predicate<Transaction> isFood =
                t -> t.getCategory() == Category.FOOD;

        Predicate<Transaction> isFoodExpense =
                isExpense.and(isFood);

        Predicate<Transaction> isLarge =
                t -> t.getAmount().compareTo(new BigDecimal("1000.00")) > 0;

        Predicate<Transaction> isLargeExpense =
                isExpense.and(isLarge);

        TransactionFormatter formatter = t ->
                t.getDate() +" | " + t.getCategory()
                        + " | " + t.getTitle() + " | " + t.getAmount();

        // Вызовите каждый интерфейс здесь.
        boolean expense = isExpense.test(transaction);
        BigDecimal amount = getAmount.apply(transaction);
        printTitle.accept(transaction);
        List<Transaction> list = createList.get();
        BigDecimal sum = addAmounts.apply(amount, amount);
        boolean foodExpense = isFoodExpense.test(transaction);
        String text = formatter.format(transaction);
        System.out.println(text);

        List<Transaction> transactions = List.of(
                new Transaction(
                        "Зарплата", new BigDecimal("50000.00"),
                        TransactionType.INCOME,
                        LocalDate.of(2026, 10, 1), Category.SALARY
                ),
                new Transaction(
                        "Продукты", new BigDecimal("1500.00"),
                        TransactionType.EXPENSE,
                        LocalDate.of(2026, 10, 2), Category.FOOD
                ),
                new Transaction(
                        "Проезд", new BigDecimal("100.00"),
                        TransactionType.EXPENSE,
                        LocalDate.of(2026, 10, 3), Category.TRANSPORT
                )
        );

        List<Transaction> expenses = transactions.stream()
                .peek(t -> System.out.println(
                        "До filter: " + t.getTitle()
                ))
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .peek(t -> System.out.println(
                        "После filter: " + t.getTitle()
                ))
                .toList();
        System.out.println("Checked transitions");

        List<Transaction> firstBatch = List.of(
                transactions.get(0), // Зарплата
                transactions.get(1)  // Продукты
        );

        List<Transaction> secondBatch = List.of(
                transactions.get(2)  // Проезд
        );

        List<List<Transaction>> batches = List.of(
                firstBatch,
                secondBatch
        );


        Stream<Stream<Transaction>> nested = batches.stream()
                .map(List::stream);
        List<Transaction> allTransactions = batches.stream()
                .peek(batch -> System.out.println(
                        "Пакет: " + batch.size() + " транзакций"
                ))
                .flatMap(List::stream)
                .peek(t -> System.out.println(
                        "Транзакция: " + t.getTitle()
                ))
                .filter(isExpense)
                .peek(t -> System.out.println("Расход: " + t.getTitle()))
                .toList();

        BigDecimal totalExpenses = transactions.stream()
                .filter(isExpense).map(Transaction::getAmount)
                .reduce(new BigDecimal("0.00"),BigDecimal::add);
        System.out.println(totalExpenses);

        Map<Category, List<Transaction>> expensesByCategory = transactions.stream()
                .filter(isExpense)
                .collect(Collectors.groupingBy(Transaction::getCategory));

        Map<Category, BigDecimal> totalsByCategory = transactions.stream()
                .filter(isExpense)
                .collect(Collectors.toMap(
                        Transaction::getCategory,
                        Transaction::getAmount,
                        BigDecimal::add,
                        LinkedHashMap::new
                ));

        Optional<String> title = Optional.empty();

        System.out.println("orElse:");
        System.out.println(title.orElse(fallbackTitle()));

        System.out.println("orElseGet:");
        System.out.println(title.orElseGet(() -> fallbackTitle()));


    }
    private static String fallbackTitle() {
        System.out.println("Вычисляем запасное название");
        return "Без названия";
    }


}
