package com.learning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class SerializationPractice {

    static final class ExportSettings implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String fileName;
        private transient String sessionNote;

        ExportSettings(String fileName, String  sessionNote) {
            this.fileName = fileName;
            this.sessionNote = sessionNote;
        }

        public String getFileName() {
            return fileName;
        }

        public String getSessionNote() {
            return sessionNote;
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ExportSettings original =
                new ExportSettings("report.csv", "Учебный запуск");

        Path path = Files.createTempFile("export-settings-", ".bin");

        try {
            try (ObjectOutputStream out =
                         new ObjectOutputStream(Files.newOutputStream(path))) {
                out.writeObject(original);
            }

            ExportSettings restored;
            try (ObjectInputStream in =
                         new ObjectInputStream(Files.newInputStream(path))) {
                restored = (ExportSettings) in.readObject();
            }
            System.out.println(original == restored);
            System.out.println(original.getFileName());
            System.out.println(restored.getFileName());
            System.out.println(original.getSessionNote());
            System.out.println(restored.getSessionNote());

        } finally {
            Files.deleteIfExists(path);
        }

    }
}
