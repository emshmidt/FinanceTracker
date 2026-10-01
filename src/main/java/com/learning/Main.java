package com.learning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

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
    }
}