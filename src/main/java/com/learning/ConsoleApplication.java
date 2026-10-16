package com.learning;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleApplication {
    private final TransactionService service;
    private final TransactionFormatter formatter;
    private final Scanner input;
    private final PrintStream output;
    private final Path storagePath;
    private final PrintStream error;

    public ConsoleApplication(
            TransactionService service,
            TransactionFormatter formatter,
            Scanner input,
            PrintStream output,
            Path storagePath,
            PrintStream error
    ) {
        if (service == null) {
            throw new IllegalArgumentException("Service must not be null");
        }
        if (formatter == null) {
            throw new IllegalArgumentException("Formatter must not be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("Input must not be null");
        }
        if (output == null) {
            throw new IllegalArgumentException("Output must not be null");
        }
        if (storagePath == null) {
            throw new IllegalArgumentException("Storage path must not be null");
        }
        if (error == null) {
            throw new IllegalArgumentException("Error message must not be null");
        }
        this.service = service;
        this.formatter = formatter;
        this.input = input;
        this.output = output;
        this.storagePath = storagePath;
        this.error = error;
    }

    private void showMenu() {
        output.println("1 — Добавить операцию\n" +
                "2 — Показать операции\n" +
                "3 — Показать баланс\n" +
                "4 — Удалить операцию\n" +
                "0 — Выйти");
    }

    private void showTransactions() {
        List<Transaction> transactions = service.findAll();
        if (transactions.isEmpty()) {
            output.println("Операций пока нет");
        } else {
            for (Transaction transaction: transactions) {
                output.println(transaction.getId() + " | " + formatter.format(transaction));
            }
        }
    }

    private void showBalance() {
        output.println("Баланс: " + service.calculateBalance());
    }

    private void showAdditionalReports() {
        output.println("Расходы по категориям: " + service.expensesByCategory());
        output.println(
                "Самый затратный месяц: "
                        + service.findMonthWithLargestExpenses()
                        .map(month -> month.toString())
                        .orElse("Нет расходов")
        );

        output.println(
                "Самая затратная категория: "
                        + service.findMostExpensiveCategory()
                        .map(category -> category.name())
                        .orElse("Нет расходов")
        );

        output.println(
                "Средний расход: "
                        + service.calculateAverageExpense()
                        .map(amount -> amount.toPlainString())
                        .orElse("Нет расходов")
        );
        output.println(
                "Два крупнейших расхода: "
                        + service.findLargestExpenses(2).stream()
                        .map(Transaction::getAmount)
                        .toList()
        );
    }

    public void run() {
        while (true) {
            showMenu();

            if (!input.hasNextLine()) {
                return;
            }

            String command = input.nextLine().trim();

            switch (command) {
                case "0":
                    return;
                case "1":
                    if (! readTransaction()) {
                        return;
                    }
                    break;
                case "2":
                    showTransactions();
                    break;
                case "3":
                    showBalance();
                    break;
                case "4":
                    if (!deleteTransaction()) {
                        return;
                    }
                    break;
                default:
                    output.println("Введите номер команды из списка");
            }
        }
    }

    private Optional<String> readTitle() {
        output.println("Введите описание транзакции");

        while (true) {
            if (!input.hasNextLine()) {
                return Optional.empty();
            }
            String text = input.nextLine().trim();

            if (text.isBlank()) {
                output.println("Описание не может быть пустым");
                continue;
            }
            return Optional.of(text);
        }
    }

    private Optional<BigDecimal> readAmount() {
        output.println("Введите положительную сумму формата: 150.50");

        while (true) {
            if (!input.hasNextLine()) {
                return Optional.empty();
            }

            String text = input.nextLine().trim();

            try {
                BigDecimal amount = new BigDecimal(text);

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    output.println("Сумма должна быть положительной");
                    continue;
                }

                if (amount.scale() > 2) {
                    output.println("Сумма должна содержать не более 2 знаков после точки");
                    continue;
                }

                return Optional.of(amount);
            } catch (NumberFormatException e) {
                output.println("Некорректное число. Попробуйте еще раз");
            }
        }
    }

    private Optional<TransactionType> readType() {
        while (true) {
            output.println("Выберите тип транзакции из списка:\n"
                    + "1 — Доход INCOME,\n"
                    + "2 — Расход EXPENSE");

            if (!input.hasNextLine()) {
                return Optional.empty();
            }

            String text = input.nextLine().trim();

            switch (text) {
                case "1":
                    return Optional.of(TransactionType.INCOME);
                case "2":
                    return Optional.of(TransactionType.EXPENSE);
                default:
                    output.println("Некорректный тип. Введите 1 или 2");
            }
        }
    }

    private Optional<LocalDate> readDate() {
        while (true) {
            output.println("Введите дату формата: 2026-10-15");

            if (!input.hasNextLine()) {
                return Optional.empty();
            }

            String text = input.nextLine().trim();

            try {
                LocalDate date = LocalDate.parse(text);
                return Optional.of(date);
            } catch (DateTimeParseException e) {
                output.println("Некорректная дата. Попробуйте еще раз");
            }
        }
    }

    private Optional<Category> readCategory() {
        while (true) {
            output.println("Выберите категорию транзакции из списка:\n"
                    + "1 — Зарплата SALARY,\n" +
                    "2 — Аренда RENT,\n" +
                    "3 — Транспорт TRANSPORT,\n" +
                    "4 — Дополнительный доход FREELANCE,\n" +
                    "5 — Еда FOOD,\n" +
                    "6 — Коммунальные услуги UTILITIES,\n" +
                    "7 — Другое OTHER");

            if (!input.hasNextLine()) {
                return Optional.empty();
            }

            String text = input.nextLine().trim();

            switch (text) {
                case "1":
                    return Optional.of(Category.SALARY);
                case "2":
                    return Optional.of(Category.RENT);
                case "3":
                    return Optional.of(Category.TRANSPORT);
                case "4":
                    return Optional.of(Category.FREELANCE);
                case "5":
                    return Optional.of(Category.FOOD);
                case "6":
                    return Optional.of(Category.UTILITIES);
                case "7":
                    return Optional.of(Category.OTHER);
                default:
                    output.println("Некорректная категория. Введите номер команды из списка");
            }
        }
    }

    private Optional<UUID> readId() {
        while (true) {
            output.println("Введите id формата: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.\n"
                    + "Например: 550e8400-e29b-41d4-a716-446655440000");

            if (!input.hasNextLine()) {
                return Optional.empty();
            }

            String text = input.nextLine().trim();

            try {
                UUID id = UUID.fromString(text);
                return Optional.of(id);

            } catch (IllegalArgumentException e) {
                output.println("Некорректный id. Попробуйте еще раз");
            }

        }
    }

    private boolean readTransaction() {
        Optional<String> title = readTitle();

        if (title.isEmpty()) {
            return false;
        }

        Optional<BigDecimal> amount = readAmount();

        if (amount.isEmpty()) {
            return false;
        }

        Optional<TransactionType> type = readType();

        if (type.isEmpty()) {
            return false;
        }

        Optional<LocalDate> date = readDate();

        if (date.isEmpty()) {
            return false;
        }

        Optional<Category> category = readCategory();

        if (category.isEmpty()) {
            return false;
        }

        Transaction transaction = new Transaction(
                title.get(), amount.get(), type.get(), date.get(), category.get()
        );

        try {
            service.add(transaction);
        } catch (UncheckedIOException e) {
            error.println(
                    "Не удалось сохранить добавление в " + storagePath
                            + ": " + e.getCause().getMessage()
            );
            return true;
        }

        output.println("Транзакция успешно добавлена!");
        return true;
    }

    private boolean deleteTransaction() {
        Optional<UUID> id = readId();

        if (id.isEmpty()) {
            return false;
        }

        try {
            if (service.removeById(id.get())) {
                output.println("Удаление прошло успешно!");
            } else {
                output.println("Транзакция с указанным UUID не найдена");
            }
        } catch (UncheckedIOException e) {
            error.println(
                    "Не удалось сохранить удаление в " + storagePath
                            + ": " + e.getCause().getMessage()
            );
        }


        return true;
    }

}
