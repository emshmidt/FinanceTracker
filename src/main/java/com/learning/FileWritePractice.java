package com.learning;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileWritePractice {
    public static void main(String[] args) {
        Path path = Path.of("data", "io-practice.txt");

        try {
            Files.createDirectories(path.getParent());

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 path, StandardCharsets.UTF_8)) {
                writer.write("Первая строка");
                writer.newLine();
                writer.write("Вторая строка");
            }
        } catch (IOException e) {
            System.out.println(
                    "Can not write file: " + e.getMessage());
        }
    }
}
