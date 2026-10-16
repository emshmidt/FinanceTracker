package com.learning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleApplicationTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldAddTransactionsShowThemAndCalculateBalance() {
        String inputText = """
        1
        Зарплата
        1000.00
        1
        2026-10-15
        1
        1
        Продукты
        150.50
        2
        2026-10-15
        5
        2
        3
        0
        """;
        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());
        List<Transaction> transactions = service.findAll();
        assertEquals(2, transactions.size());

        Transaction salary = transactions.get(0);

        assertAll(
                () -> assertEquals("Зарплата", salary.getTitle()),
                () -> assertEquals(new BigDecimal("1000.00"), salary.getAmount()),
                () -> assertEquals(TransactionType.INCOME, salary.getType()),
                () -> assertEquals(LocalDate.of(2026, 10, 15), salary.getDate()),
                () -> assertEquals(Category.SALARY, salary.getCategory()),
                () -> assertNotNull(salary.getId())
        );

        Transaction expense = transactions.get(1);

        assertAll(
                () -> assertEquals("Продукты", expense.getTitle()),
                () -> assertEquals(new BigDecimal("150.50"), expense.getAmount()),
                () -> assertEquals(TransactionType.EXPENSE, expense.getType()),
                () -> assertEquals(LocalDate.of(2026, 10, 15), expense.getDate()),
                () -> assertEquals(Category.FOOD, expense.getCategory()),
                () -> assertNotNull(expense.getId())
        );

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(text.contains("Зарплата")),
                () -> assertTrue(text.contains("Продукты")),
                () -> assertTrue(text.contains(salary.getId().toString())),
                () -> assertTrue(text.contains(expense.getId().toString())),
                () -> assertTrue(text.contains("Баланс: 849.50"))
        );

        long successCount = text.lines()
                .filter(line -> line.equals("Транзакция успешно добавлена!"))
                .count();

        assertEquals(2L, successCount);
    }

    @Test
    void shouldShowMessageWhenNoTransactionsExist() {
        String inputText = "2\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Операций пока нет"));
    }

    @Test
    void shouldShowZeroBalanceWhenNoTransactionsExist(){
        String inputText = "3\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Баланс: 0.00"));
    }

    @Test
    void shouldContinueAfterUnknownCommand() {
        String inputText = "abc\n9\n\n3\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Введите номер команды из списка"));

        assertTrue(text.contains("Баланс: "));
    }

    @Test
    void shouldStopAfterExitCommand() {
        String inputText = "0\n3\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertFalse(text.contains("Баланс: "));
    }

    @Test
    void shouldFinishWhenInputEndsInMenu() {
        String inputText = "";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        assertDoesNotThrow(application::run);
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());
        assertEquals(List.of(), service.findAll());
    }

    @Test
    void shouldRetryBlankTitle() {
        String inputText = "1\n\n   \nПродукты\n150.50\n2\n2026-10-15\n5\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Описание не может быть пустым"));

        List<Transaction> transactions = service.findAll();

        assertEquals(1, transactions.size());
        assertEquals("Продукты", transactions.getFirst().getTitle());
        assertEquals(new BigDecimal("150.50"), transactions.getFirst().getAmount());
        assertEquals(TransactionType.EXPENSE, transactions.getFirst().getType());
        assertEquals(LocalDate.of(2026, 10, 15), transactions.getFirst().getDate());
        assertEquals(Category.FOOD, transactions.getFirst().getCategory());
    }

    @Test
    void shouldRetryInvalidAmount() {
        String inputText = "1\nПродукты\nabc\n0\n-10\n1.234\n150.50\n2\n2026-10-15\n5\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Некорректное число. Попробуйте еще раз"));
        assertTrue(text.contains("Сумма должна быть положительной"));
        assertTrue(text.contains("Сумма должна содержать не более 2 знаков после точки"));

        List<Transaction> transactions = service.findAll();

        assertEquals(1, transactions.size());
        assertEquals("Продукты", transactions.getFirst().getTitle());
        assertEquals(new BigDecimal("150.50"), transactions.getFirst().getAmount());
        assertEquals(TransactionType.EXPENSE, transactions.getFirst().getType());
        assertEquals(LocalDate.of(2026, 10, 15), transactions.getFirst().getDate());
        assertEquals(Category.FOOD, transactions.getFirst().getCategory());
    }

    @Test
    void shouldRetryInvalidType() {
        String inputText = "1\nПродукты\n150.50\nabc\n9\n2\n2026-10-15\n5\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Некорректный тип. Введите 1 или 2"));

        List<Transaction> transactions = service.findAll();

        assertEquals(1, transactions.size());
        assertEquals("Продукты", transactions.getFirst().getTitle());
        assertEquals(new BigDecimal("150.50"), transactions.getFirst().getAmount());
        assertEquals(TransactionType.EXPENSE, transactions.getFirst().getType());
        assertEquals(LocalDate.of(2026, 10, 15), transactions.getFirst().getDate());
        assertEquals(Category.FOOD, transactions.getFirst().getCategory());
    }

    @Test
    void shouldRetryInvalidDate() {
        String inputText = "1\nПродукты\n150.50\n2\nabc\n2026-02-30\n2026-10-15\n5\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Некорректная дата. Попробуйте еще раз"));

        List<Transaction> transactions = service.findAll();

        assertEquals(1, transactions.size());
        assertEquals("Продукты", transactions.getFirst().getTitle());
        assertEquals(new BigDecimal("150.50"), transactions.getFirst().getAmount());
        assertEquals(TransactionType.EXPENSE, transactions.getFirst().getType());
        assertEquals(LocalDate.of(2026, 10, 15), transactions.getFirst().getDate());
        assertEquals(Category.FOOD, transactions.getFirst().getCategory());
    }

    @Test
    void shouldRetryInvalidCategory() {
        String inputText = "1\nПродукты\n150.50\n2\n2026-10-15\nabc\n99\n5\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Некорректная категория. Введите номер команды из списка"));

        List<Transaction> transactions = service.findAll();

        assertEquals(1, transactions.size());
        assertEquals("Продукты", transactions.getFirst().getTitle());
        assertEquals(new BigDecimal("150.50"), transactions.getFirst().getAmount());
        assertEquals(TransactionType.EXPENSE, transactions.getFirst().getType());
        assertEquals(LocalDate.of(2026, 10, 15), transactions.getFirst().getDate());
        assertEquals(Category.FOOD, transactions.getFirst().getCategory());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1\n",
            "1\nПродукты\n",
            "1\nПродукты\n150.50\n",
            "1\nПродукты\n150.50\n2\n",
            "1\nПродукты\n150.50\n2\n2026-10-15\n"
    })
    void shouldNotSaveIncompleteTransactionWhenInputEnds(String inputText) {
        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );
        assertDoesNotThrow(application::run);
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);
        assertFalse(text.contains("Транзакция успешно добавлена!"));

        List<Transaction> transactions = service.findAll();
        assertEquals(List.of(), transactions);
    }

    @Test
    void shouldNotRequestRemainingFieldsAfterInputEnds() {
        String inputText = "1\nПродукты\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Введите положительную сумму формата:"));
        assertFalse(text.contains("Выберите тип транзакции из списка:"));
        assertFalse(text.contains("Введите дату формата:"));
        assertFalse(text.contains("Выберите категорию транзакции из списка:"));
    }

    @Test
    void shouldDeleteTransactionById() {
        Transaction salary = new Transaction(
                "Зарплата", new BigDecimal("1000.00"), TransactionType.INCOME,
                LocalDate.of(2025, 10, 15), Category.SALARY
        );

        Transaction expense = new Transaction(
                "КФС", new BigDecimal("150.50"), TransactionType.EXPENSE,
                LocalDate.of(2025, 10, 15), Category.FOOD
        );

        String inputText = "4\n" + expense.getId() + "\n2\n3\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );
        service.add(salary);
        service.add(expense);

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        List<Transaction> transactions = service.findAll();

        assertEquals(List.of(salary), transactions);

        assertTrue(text.contains("Удаление прошло успешно!"));
        assertTrue(text.contains("Баланс: 1000.00"));
        assertTrue(text.contains(salary.getId().toString()));
    }

    @Test
    void shouldRetryInvalidUuidAndDeleteTransaction() {
        Transaction salary = new Transaction(
                "Зарплата", new BigDecimal("1000.00"), TransactionType.INCOME,
                LocalDate.of(2025, 10, 15), Category.SALARY
        );

        Transaction expense = new Transaction(
                "КФС", new BigDecimal("150.50"), TransactionType.EXPENSE,
                LocalDate.of(2025, 10, 15), Category.FOOD
        );

        String inputText = "4\nwrong-id\n" + expense.getId() + "\n3\n0\n";

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );
        service.add(salary);
        service.add(expense);

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        List<Transaction> transactions = service.findAll();

        assertEquals(List.of(salary), transactions);

        assertTrue(text.contains("Некорректный id. Попробуйте еще раз"));
        assertTrue(text.contains("Удаление прошло успешно!"));
        assertTrue(text.contains("Баланс: 1000.00"));
    }

    @Test
    void shouldContinueAfterDeletingUnknownTransaction() {
        String inputText = """
        4
        550e8400-e29b-41d4-a716-446655440000
        3
        0
        """;

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );

        application.run();
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        assertTrue(text.contains("Транзакция с указанным UUID не найдена"));
        assertFalse(text.contains("Удаление прошло успешно!"));
        assertTrue(text.contains("Баланс: "));
        assertEquals(List.of(), service.findAll());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "4\n",
            "4\nwrong-id\n"
    })
    void shouldNotDeleteTransactionWhenInputEnds(String inputText) {
        Transaction salary = new Transaction(
                "Зарплата", new BigDecimal("1000.00"), TransactionType.INCOME,
                LocalDate.of(2025, 10, 15), Category.SALARY
        );

        TransactionService service =
                new TransactionService(new InMemoryTransactionRepository());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
        PrintStream error = new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner scanner = new Scanner(inputText);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                scanner,
                output,
                Path.of("test-transactions.csv"),
                error
        );
        service.add(salary);

        assertDoesNotThrow(application::run);
        assertTrue(errorBuffer.toString(StandardCharsets.UTF_8).isEmpty());

        String text = buffer.toString(StandardCharsets.UTF_8);

        List<Transaction> transactions = service.findAll();

        assertEquals(List.of(salary), transactions);
        assertFalse(text.contains("Удаление прошло успешно!"));
    }

    @Test
    void shouldContinueMenuWhenSavingAddedTransactionFails() throws IOException {
        Path file = tempDir.resolve("transactions.csv");

        TransactionService service = new TransactionService(
                new FileTransactionRepository(file)
        );

        Transaction salary = new Transaction(
                "Зарплата",
                new BigDecimal("1000.00"),
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 15),
                Category.SALARY
        );
        service.add(salary);

        List<Transaction> before = service.findAll();

        Files.delete(file);
        Files.createDirectory(file);
        Files.writeString(file.resolve("marker.txt"), "busy");

        String inputText = """
            1
            Продукты
            150.50
            2
            2026-10-15
            5
            2
            3
            0
            """;

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                new Scanner(inputText),
                output,
                file,
                error
        );

        assertDoesNotThrow(application::run);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);


        assertEquals(before, service.findAll());

        String prefix = "Не удалось сохранить добавление в " + file + ": ";
        assertTrue(errorText.startsWith(prefix));

        String reason = errorText.substring(prefix.length()).trim();
        assertFalse(reason.isBlank());

        assertFalse(outputText.contains("Транзакция успешно добавлена!"));

        assertTrue(outputText.contains(salary.getId().toString()));
        assertTrue(outputText.contains("Баланс: 1000.00"));

        assertEquals("busy", Files.readString(file.resolve("marker.txt")));
    }

    @Test
    void shouldContinueMenuWhenSavingDeletionFails() throws IOException{
        Path file = tempDir.resolve("transactions.csv");

        TransactionService service = new TransactionService(
                new FileTransactionRepository(file)
        );

        Transaction salary = new Transaction(
                "Зарплата",
                new BigDecimal("1000.00"),
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 15),
                Category.SALARY
        );
        Transaction expense = new Transaction(
                "Покупки",
                new BigDecimal("150.50"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 1),
                Category.OTHER
        );
        service.add(salary);
        service.add(expense);

        List<Transaction> before = service.findAll();

        Files.delete(file);
        Files.createDirectory(file);
        Files.writeString(file.resolve("marker.txt"), "busy");

        String inputText = "4\n" + expense.getId() + "\n2\n3\n0\n";

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        ConsoleApplication application = new ConsoleApplication(
                service,
                new ConsoleTransactionFormatter(),
                new Scanner(inputText),
                output,
                file,
                error
        );

        assertDoesNotThrow(application::run);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);


        assertEquals(before, service.findAll());

        String prefix = "Не удалось сохранить удаление в " + file + ": ";
        assertTrue(errorText.startsWith(prefix));

        String reason = errorText.substring(prefix.length()).trim();
        assertFalse(reason.isBlank());

        assertFalse(outputText.contains("Удаление прошло успешно!"));

        assertTrue(outputText.contains(salary.getId().toString()));
        assertTrue(outputText.contains(expense.getId().toString()));
        assertTrue(outputText.contains("Баланс: 849.50"));

        assertEquals("busy", Files.readString(file.resolve("marker.txt")));

    }
}
