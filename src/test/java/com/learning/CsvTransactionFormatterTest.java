package com.learning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvTransactionFormatterTest {
    CsvTransactionFormatter formatter;

    @BeforeEach
    void setup() {
        formatter = new CsvTransactionFormatter();
    }

    @Test
    void shouldFormatAllTransactionFieldsInExpectedOrder() {
        Transaction transaction = new Transaction(
                "Продукты",
                new BigDecimal("150.50"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 7),
                Category.FOOD
        );
        String expected = "\""+ transaction.getId().toString() + "\"" + ",\"Продукты\"," +
                "\"150.50\",\"EXPENSE\",\"2026-10-07\",\"FOOD\"";

        String result = formatter.format(transaction);

        assertEquals(expected, result);
    }

    @Test
    void shouldPreserveCyrillicTitle() {
        Transaction transaction = new Transaction(
                "Продукты и кофе",
                new BigDecimal("150.50"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 7),
                Category.FOOD
        );
        String expected = "\""+ transaction.getId().toString() + "\"" + ",\"Продукты и кофе\"," +
                "\"150.50\",\"EXPENSE\",\"2026-10-07\",\"FOOD\"";

        String result = formatter.format(transaction);

        assertEquals(expected, result);
    }

    @Test
    void shouldQuoteTitleContainingComma() {
        Transaction transaction = new Transaction(
                "Продукты, кофе",
                new BigDecimal("150.50"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 7),
                Category.FOOD
        );
        String expected = "\""+ transaction.getId().toString() + "\"" + ",\"Продукты, кофе\"," +
                "\"150.50\",\"EXPENSE\",\"2026-10-07\",\"FOOD\"";

        String result = formatter.format(transaction);

        assertEquals(expected, result);
    }

    @Test
    void shouldEscapeQuotesInTitle() {
        Transaction transaction = new Transaction(
                "Продукты " + "\"" + "Лента" + "\"",
                new BigDecimal("150.50"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 7),
                Category.FOOD
        );
        String expected = "\""+ transaction.getId().toString() + "\"" + ",\"Продукты \"\"Лента\"\"\"," +
                "\"150.50\",\"EXPENSE\",\"2026-10-07\",\"FOOD\"";

        String result = formatter.format(transaction);

        assertEquals(expected, result);
    }

    @Test
    void shouldPreserveLineBreakInsideQuotedTitle() {
        Transaction transaction = new Transaction(
                "Продукты\nЛента",
                new BigDecimal("150.50"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 7),
                Category.FOOD
        );
        String expected = "\""+ transaction.getId().toString() + "\"" + ",\"Продукты\nЛента\","
                + "\"150.50\",\"EXPENSE\",\"2026-10-07\",\"FOOD\"";

        String result = formatter.format(transaction);

        assertEquals(expected, result);
    }

    @Test
    void shouldFormatAmountWithTwoDecimalPlaces() {
        Transaction transaction = new Transaction(
                "Продукты и кофе",
                new BigDecimal("150.5"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 7),
                Category.FOOD
        );
        String expected = "\""+ transaction.getId().toString() + "\"" + ",\"Продукты и кофе\"," +
                "\"150.50\",\"EXPENSE\",\"2026-10-07\",\"FOOD\"";

        String result = formatter.format(transaction);

        assertEquals(expected, result);
    }
}
