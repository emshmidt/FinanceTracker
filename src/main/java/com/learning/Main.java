package com.learning;

import javax.print.attribute.standard.Severity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.10"), TransactionType.INCOME, LocalDate.of(2025, 1, 31), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("33.33"), TransactionType.EXPENSE, LocalDate.of(2024, 2, 29), Category.FOOD);

        transactionService.add(transactionSalary);
        transactionService.add(transactionFood);


        System.out.println(transactionService.calculateBalance());

        System.out.println(new BigDecimal("0.1").compareTo(new BigDecimal(0.1)) == 0);
        System.out.println(new BigDecimal("0.1"));
        System.out.println(new BigDecimal(0.1));
        System.out.println(new BigDecimal("10.0").equals(new BigDecimal("10.00")));
        System.out.println(new BigDecimal("10.0").compareTo(new BigDecimal("10.00")) == 0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        List<Transaction> transactions = transactionService.findAll();

        Stream<Transaction> transactionStream= transactions.stream();
        Stream<String> titleStream = transactions.stream()
                .map(Transaction::getTitle);
        Stream<BigDecimal> amountStream = transactions.stream()
                .map(Transaction::getAmount);




        for (Transaction transaction: transactions) {
            System.out.println(transaction.getCategory()+ " Date: " + transaction.getDate().format(formatter));
        }
        for (Transaction transaction: transactions) {
            System.out.println(transaction.getCategory()+ " 1%: " + transaction.getAmount()
                    .divide(new BigDecimal("100.00"), 2, RoundingMode.HALF_UP));
        }
        try {
            transactionService.getByIdOrThrow(UUID.randomUUID());
        } catch (TransactionNotFoundException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        try {
            Transaction newTransaction = new Transaction(null, new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.now(), Category.SALARY);
        } catch (InvalidTransactionException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        TransactionService service = new TransactionService();

        Transaction foodJanuary = new Transaction(
                "Продукты в январе",
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 10),
                Category.FOOD
        );

        Transaction transportJanuary = new Transaction(
                "Проезд в январе",
                new BigDecimal("50.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 20),
                Category.TRANSPORT
        );

        Transaction foodFebruary = new Transaction(
                "Продукты в феврале",
                new BigDecimal("200.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 2, 5),
                Category.FOOD
        );

        Transaction salaryFebruary = new Transaction(
                "Зарплата в феврале",
                new BigDecimal("1000.00"),
                TransactionType.INCOME,
                LocalDate.of(2025, 2, 6),
                Category.SALARY
        );

        Transaction foodNextYear = new Transaction(
                "Продукты в следующем году",
                new BigDecimal("10.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 1, 10),
                Category.FOOD
        );

        service.add(foodJanuary);
        service.add(transportJanuary);
        service.add(foodFebruary);
        service.add(salaryFebruary);
        service.add(foodNextYear);

        System.out.println(service.expensesByCategory());
        System.out.println(service.findMonthWithLargestExpenses());
        System.out.println(service.findMostExpensiveCategory());
        System.out.println(service.calculateAverageExpense());System.out.println(
                service.findLargestExpenses(2).stream()
                        .map(Transaction::getAmount)
                        .toList()
        );
    }
}