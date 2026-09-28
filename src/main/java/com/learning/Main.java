package com.learning;

import java.math.BigDecimal;
import java.time.LocalDate;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        TransactionService transactionService = new TransactionService();
        Transaction transactionSalary = new Transaction("a", new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.now(), Category.SALARY);
        Transaction transactionFood = new Transaction("b", new BigDecimal("30.00"), TransactionType.EXPENSE, LocalDate.now(), Category.FOOD);

        transactionService.add(transactionSalary);
        transactionService.add(transactionFood);
        System.out.println(transactionService.calculateBalance());
    }
}