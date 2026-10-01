package com.learning;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FileReadPractice {
    public static void main(String[] args) {
        Path path = Path.of("dates.txt");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    LocalDate date = LocalDate.parse(line);
                    System.out.println(date.format(formatter));
                } catch (DateTimeParseException e) {
                    System.out.println("Wrong date: "+ line);
                }
            }
        } catch (IOException e) {
            System.out.println("Can't read file: " + e.getMessage());
        }
    }
}
