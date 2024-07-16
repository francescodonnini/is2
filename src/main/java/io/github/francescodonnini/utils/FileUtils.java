package io.github.francescodonnini.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {
    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    private FileUtils() {}

    public static void createFileIfNotExists(String path) {
        var file = new File(path);
        var message = "directory %s already exists".formatted(file.getParentFile());
        if (file.getParentFile().mkdirs()) {
            message = "directory %s has been created.".formatted(file.getParentFile());
        }
        logger.log(Level.INFO, message);
    }

    public static List<Path> listAllFiles(Path basePath) throws IOException {
        var files = new ArrayList<Path>();
        listAllFiles(basePath, basePath, files);
        return files;
    }

    public static void listAllFiles(Path basePath, Path currentPath, List<Path> allFiles) throws IOException
    {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath))
        {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    if (!Files.isHidden(entry))
                        listAllFiles(basePath, entry, allFiles);
                } else {
                    allFiles.add(basePath.relativize(entry));
                }
            }
        }
    }

    public static boolean isJavaNonTestFile(String path) {
        return isJavaFile(path) && isNonTestFile(path);
    }

    public static boolean isJavaFile(String path) {
        return path.endsWith(".java");
    }

    public static boolean isNonTestFile(String path) {
        return path.contains("src/main");
    }
}
