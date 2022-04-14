package org.harryng.demo.natives;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileAccession {
    static Logger logger = LoggerFactory.getLogger(FileAccession.class);
    static String dirName = "./files";
    static String fileName = "test.txt";

    public void writeFile() throws IOException {
        var dirPath = Paths.get(dirName);
        if (!Files.exists(dirPath)) {
            Files.createDirectory(dirPath);
            logger.info("Dir is created!");
        }
        var filePath = Paths.get(String.format("%s/%s", dirName, fileName));
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        Files.writeString(filePath, "test data", StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void readFile() throws IOException {
        var filePath = Paths.get(String.format("%s/%s", dirName, fileName));
        if (Files.exists(filePath)) {
            var str = Files.readString(filePath, StandardCharsets.UTF_8);
            logger.info(str);
        }
    }
}
