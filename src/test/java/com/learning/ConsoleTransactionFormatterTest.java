package com.learning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleTransactionFormatterTest {

    @Test
    void shouldFormatTransactionForConsole() {
        Transaction transaction = new Transaction(
                "Продукты в январе",
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2025, 1, 10),
                Category.FOOD
        );
        TransactionFormatter formatter = new ConsoleTransactionFormatter();

        String text = formatter.format(transaction);

        assertEquals(
                "10.01.2025 | FOOD | Продукты в январе | 100.00",
                text
        );
    }
}