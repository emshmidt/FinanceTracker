package com.learning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    @TempDir
    Path tempDir;

    @ParameterizedTest
    @MethodSource("testData")
    void shouldResolvePath(String[] args, Path expected) {
        assertEquals(expected, Main.resolveStoragePath(args));
    }

    static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of(
                        new String[]{},
                        Path.of("data", "transactions.csv")
                ),
                Arguments.of(
                        new String[]{""},
                        Path.of("data", "transactions.csv")
                ),
                Arguments.of(
                        new String[]{"   "},
                        Path.of("data", "transactions.csv")
                ),
                Arguments.of(
                        new String[]{"data/my transactions.csv"},
                        Path.of("data/my transactions.csv")
                )
        );
    }

    @Test
    void shouldThrowWhenMoreThanOneArgumentProvided() {
        String[] args = new String[]{"a.csv", "b.csv"};

        assertThrows(
                IllegalArgumentException.class,
                () -> Main.resolveStoragePath(args)
        );
    }

    @Test
    void shouldRejectExtraArgumentsWithoutStartingMenu() {
        String[] args = {"first.csv", "second.csv"};

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner input = new Scanner("3\n0\n");

        int status = Main.run(args, input, output, error);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);

        assertEquals(1, status);
        assertTrue(errorText.contains("Ошибка аргументов"));
        assertFalse(outputText.contains("1 — Добавить операцию"));
    }

    @Test
    void shouldRejectInvalidPathWithoutStartingMenu() {
        String invalidPath = "bad" + (char) 0 + ".csv";
        String[] args = {invalidPath};

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner input = new Scanner("3\n0\n");

        int status = Main.run(args, input, output, error);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);

        assertEquals(1, status);
        assertTrue(errorText.contains("Недопустимый путь"));
        assertFalse(outputText.contains("1 — Добавить операцию"));
        String marker = "Причина:";
        assertTrue(errorText.contains(marker));

        String reason = errorText
                .substring(errorText.indexOf(marker) + marker.length())
                .trim();

        assertFalse(reason.isBlank());
    }

    @Test
    void shouldRejectDirectoryAsStorageWithoutStartingMenu() throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("storage"));
        String[] args = {directory.toString()};

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner input = new Scanner("3\n0\n");

        int status = Main.run(args, input, output, error);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);

        assertEquals(1, status);
        assertTrue(errorText.contains(directory.toString()));
        assertFalse(outputText.contains("1 — Добавить операцию"));
        assertTrue(Files.isDirectory(directory));
        assertTrue(errorText.contains("Не удалось загрузить операции"));
    }

    @Test
    void shouldRejectInvalidCsvHeaderWithoutChangingFile() throws IOException{
        String csvText = "wrong,header\n";

        Path file = tempDir.resolve("transactions.csv");
        Files.writeString(file, csvText, StandardCharsets.UTF_8);

        byte[] before = Files.readAllBytes(file);
        String[] args = {file.toString()};

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner input = new Scanner("3\n0\n");

        int status = Main.run(args, input, output, error);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);

        assertEquals(1, status);
        assertTrue(errorText.contains("header"));
        assertTrue(errorText.contains(file.toString()));
        assertFalse(outputText.contains("1 — Добавить операцию"));
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void shouldRejectInvalidCsvFieldWithoutChangingFile() throws IOException{
        final String HEADER =
                "id,title,amount,type,date,category\n";

        final String VALID_ROW =
                "550e8400-e29b-41d4-a716-446655440000,"
                        + "Зарплата,1000.00,INCOME,2026-10-15,SALARY\n";

        String csvText = HEADER + VALID_ROW.replace("1000.00", "abc");

        Path file = tempDir.resolve("transactions.csv");
        Files.writeString(file, csvText, StandardCharsets.UTF_8);

        byte[] before = Files.readAllBytes(file);
        String[] args = {file.toString()};

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner input = new Scanner("3\n0\n");

        int status = Main.run(args, input, output, error);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);

        assertEquals(1, status);
        assertTrue(errorText.contains("amount"));
        assertTrue(errorText.contains("abc"));
        assertTrue(errorText.contains(file.toString()));
        assertFalse(outputText.contains("1 — Добавить операцию"));
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void shouldRejectDuplicateUuidInCsvWithoutChangingFile() throws IOException{
        final String HEADER =
                "id,title,amount,type,date,category\n";

        final String VALID_ROW =
                "550e8400-e29b-41d4-a716-446655440000,"
                        + "Зарплата,1000.00,INCOME,2026-10-15,SALARY\n";

        String csvText = HEADER + VALID_ROW + VALID_ROW;

        Path file = tempDir.resolve("transactions.csv");
        Files.writeString(file, csvText, StandardCharsets.UTF_8);

        byte[] before = Files.readAllBytes(file);
        String[] args = {file.toString()};

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner input = new Scanner("3\n0\n");

        int status = Main.run(args, input, output, error);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);

        assertEquals(1, status);
        assertTrue(errorText.contains("duplicate UUID"));
        assertTrue(errorText.contains(file.toString()));
        assertFalse(outputText.contains("1 — Добавить операцию"));
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void shouldRejectUnclosedCsvQuoteWithoutChangingFile() throws IOException{
        final String HEADER =
                "id,title,amount,type,date,category\n";

        String csvText = HEADER
                + "550e8400-e29b-41d4-a716-446655440000,"
                + "\"Незакрытое описание";

        Path file = tempDir.resolve("transactions.csv");
        Files.writeString(file, csvText, StandardCharsets.UTF_8);

        byte[] before = Files.readAllBytes(file);
        String[] args = {file.toString()};

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        Scanner input = new Scanner("3\n0\n");

        int status = Main.run(args, input, output, error);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);

        assertEquals(1, status);
        assertTrue(errorText.contains(file.toString()));
        assertFalse(outputText.contains("1 — Добавить операцию"));
        assertArrayEquals(before, Files.readAllBytes(file));

        String marker = "Причина:";
        assertTrue(errorText.contains(marker));

        String reason = errorText
                .substring(errorText.indexOf(marker) + marker.length())
                .trim();

        assertFalse(reason.isBlank());
    }

    @Test
    void shouldStartWithEmptyStorageWhenFileDoesNotExist() {
        Path file = tempDir.resolve("missing.csv");
        String[] args = {file.toString()};

        assertFalse(Files.exists(file));

        Scanner input = new Scanner("2\n3\n0\n");

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        PrintStream output =
                new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
        PrintStream error =
                new PrintStream(errorBuffer, true, StandardCharsets.UTF_8);

        int status = Main.run(args, input, output, error);

        String outputText = outputBuffer.toString(StandardCharsets.UTF_8);
        String errorText = errorBuffer.toString(StandardCharsets.UTF_8);

        assertEquals(0, status);
        assertTrue(errorText.isBlank());
        assertTrue(outputText.contains("1 — Добавить операцию"));
        assertTrue(outputText.contains("Операций пока нет"));
        assertTrue(outputText.contains("Баланс: 0.00"));
        assertFalse(Files.exists(file));
    }
}
