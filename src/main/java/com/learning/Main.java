package com.learning;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Path storagePath = Path.of("data", "transactions.csv");
        FileTransactionRepository fileRepository = new FileTransactionRepository(storagePath);

        TransactionService service = new TransactionService(fileRepository);

        List<Transaction> transactions = service.findAll();
        TransactionFormatter formatter = new ConsoleTransactionFormatter();

        for (Transaction transaction: transactions) {
            String text = formatter.format(transaction);

            System.out.println(text);
        }

        System.out.println("Баланс: " + service.calculateBalance());
        System.out.println("Расходы по категориям: " + service.expensesByCategory());
        System.out.println(
                "Самый затратный месяц: "
                        + service.findMonthWithLargestExpenses()
                        .map(month -> month.toString())
                        .orElse("Нет расходов")
        );

        System.out.println(
                "Самая затратная категория: "
                        + service.findMostExpensiveCategory()
                        .map(category -> category.name())
                        .orElse("Нет расходов")
        );

        System.out.println(
                "Средний расход: "
                        + service.calculateAverageExpense()
                        .map(amount -> amount.toPlainString())
                        .orElse("Нет расходов")
        );
        System.out.println(
                "Два крупнейших расхода: "
                        + service.findLargestExpenses(2).stream()
                        .map(Transaction::getAmount)
                        .toList()
        );
    }

    private void createTransactions(TransactionService service) {
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
    }

    private void exportTransactions(List<Transaction> transactions) {
        CsvTransactionExporter exporter = new CsvTransactionExporter();

        Path exportPath = Path.of("data", "transactions.csv");

        try {
            exporter.export(transactions, exportPath);
            System.out.println("Экспорт выполнен: " + exportPath);
        } catch (IOException e) {
            System.err.println(
                    "Не удалось экспортировать в " + exportPath
                            + ": " + e.getMessage());
        }
    }
}