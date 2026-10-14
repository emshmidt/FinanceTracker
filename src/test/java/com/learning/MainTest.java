package com.learning;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MainTest {
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
}
