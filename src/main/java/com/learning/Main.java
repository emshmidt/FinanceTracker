package com.learning;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static Path resolveStoragePath(String[] args) {
        if (args.length > 1) {
            throw new IllegalArgumentException(
                    "Expected at most one argument: storage path"
            );
        }

        if (args.length == 0 || args[0].isBlank()) {
            return Path.of("data", "transactions.csv");
        }

        return Path.of(args[0]);
    }

    public static void main(String[] args) {
        int status = run(
                args,
                new Scanner(System.in),
                System.out,
                System.err
        );

        if (status != 0) {
            System.exit(status);
        }

    }
    static int run(
            String[] args,
            Scanner input,
            PrintStream output,
            PrintStream error
    ) {
        Path storagePath;
        try {
            storagePath = resolveStoragePath(args);
        } catch (InvalidPathException e) {
            error.println(
                    "Недопустимый путь: " + e.getInput()
                            + ". Причина: " + e.getReason()
            );
            return 1;
        } catch (IllegalArgumentException e) {
            error.println(
                    "Ошибка аргументов: " + e.getMessage()
                            + ". Передайте не более одного аргумента — путь к CSV."
            );
            return 1;
        }
        FileTransactionRepository fileRepository;
        try {
            fileRepository = new FileTransactionRepository(storagePath);
        } catch(IllegalArgumentException e) {
            error.println(
                    "Ошибка в данных файла: " + e.getLocalizedMessage()
                    + ". Путь к файлу: " + storagePath
            );
            return 1;
        } catch (UncheckedIOException e) {
            error.println(
                    "Не удалось загрузить операции из " + storagePath
                            + ". Причина: " + e.getCause().getMessage()
            );
            return 1;
        }



        TransactionService service = new TransactionService(fileRepository);
        TransactionFormatter formatter = new ConsoleTransactionFormatter();

        ConsoleApplication application = new ConsoleApplication(
                service, formatter, input, output, storagePath, error
        );
        application.run();
        return 0;
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