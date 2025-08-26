package utility;

import model.FileWithTime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static java.util.Comparator.comparing;

public enum RenamerUtility {
    INSTANCE;

    /**
     * Validates that the given directory matches the provided folder name pattern,
     * and extracts the folder number from the first capturing group.
     *
     * @param dir           the directory path to validate
     * @param folderPattern regex pattern that must match the folder name;
     *                      group(1) should capture the numeric folder ID
     * @return the numeric folder ID as an integer
     * @throws IllegalArgumentException if the path is not a directory,
     *                                  or the folder name does not match the pattern
     * @apiNote directory name "110OLYMP" will extract 110 as int
     */
    public int extractPrefix(Path dir, Pattern folderPattern) {
        var dirName = dir.getFileName().toString();
        var m = folderPattern.matcher(dirName);
        if (!m.matches()) {
            throw new IllegalArgumentException("Directory pattern doesn't match %s".formatted(dirName));
        }
        return Integer.parseInt(m.group(1));
    }

    /**
     * Lists all files in the given directory that have one of the specified extensions.
     * Search is non-recursive.
     *
     * @param dir        the directory to scan
     * @param extensions list of extensions to match (lowercase, including the dot, e.g. ".png")
     * @return a list of matching file paths
     * @throws IOException if an I/O error occurs
     */
    public List<Path> listByExtension(Path dir, List<String> extensions) throws IOException {
        try (var files = Files.list(dir)) {
            return files
                .filter(Files::isRegularFile)
                .filter(f -> {
                    var fileName = f.getFileName().toString().toLowerCase(Locale.ROOT);
                    for (var extension : extensions) {
                        if (fileName.endsWith(extension)) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
        }
    }

    /**
     * Sorts a list of files by creation time (oldest first).
     * If creation time is not available, falls back to epoch (1970-01-01).
     *
     * @param files the list of files to sort
     * @return a new list sorted by creation time
     */
    public List<Path> sortFilesByCreationTime(List<Path> files) {
        return files.stream()
            .map(file -> {
                try {
                    var fileAttributes = Files.readAttributes(file, BasicFileAttributes.class);
                    var creationTime = fileAttributes.creationTime();
                    if (creationTime != null) {
                        return new FileWithTime(file, creationTime.toInstant());
                    }
                    return new FileWithTime(file, Instant.EPOCH);
                } catch (IOException e) {
                    System.err.printf("Error reading attributes file from %s, " +
                        "default 1970-01-01T00:00:00Z", file.getFileName());
                    return new FileWithTime(file, Instant.EPOCH);
                }
            })
            .sorted(comparing(FileWithTime::time))
            .map(FileWithTime::path)
            .toList();
    }

    /**
     * Creates a mapping from old file paths to new file paths using a sequential numbering strategy.
     *
     * @param files                list of original file paths
     * @param startSequenceNumber  starting number for the sequence
     * @param newNameSequenceFn    function that generates the new file name given the sequence number and original file
     * @return a LinkedHashMap preserving the order of the input files
     */
    public Map<Path, Path> mapSequence(
            List<Path> files,
            int startSequenceNumber,
            BiFunction<Integer, Path, String> newNameSequenceFn
    ) {
        var map = new LinkedHashMap<Path, Path>();
        var num = startSequenceNumber;
        for (var file : files) {
            var applyName = newNameSequenceFn.apply(num++, file);
            map.put(file, file.resolveSibling(applyName));
        }

        System.out.printf("Preview rename (%d file):", map.size());
        map.forEach(
            (oldName, newName) -> System.out.printf(
                "\n%s -> %s",
                oldName.getFileName(),
                newName.getFileName())
        );

        return map;
    }

    /**
     * Checks for potential conflicts in the renaming map:
     * - Duplicate target file names
     * - Target file already exists on disk and is not the same file as the source
     *
     * @param oldNameToNewNameMap map of original file path -> new file path
     * @throws IllegalStateException if a conflict is detected
     */
    public void checkConflicts(Map<Path, Path> oldNameToNewNameMap) {
        var seen = new HashSet<String>();
        oldNameToNewNameMap.forEach((oldPath, newPath) -> {
            var newFileName = newPath.getFileName().toString();
            if (!seen.add(newFileName)) {
                throw new IllegalStateException("Duplicate target file name: %s".formatted(newFileName));
            }

            try {
                if (Files.exists(newPath) && !Files.isSameFile(oldPath, newPath)) {
                    throw new IllegalStateException("File %s already exist in this directory".formatted(newPath));
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Performs a safe two-phase rename to avoid file overwrite conflicts:
     * 1. Moves each original file to a unique temporary file in the same directory.
     * 2. Moves each temporary file to its final target name.
     *
     * @param oldNameToNewNameMap map of original file path -> new file path
     */
    public void renames(Map<Path, Path> oldNameToNewNameMap, boolean dryRun) {
        if (dryRun) {
            System.out.printf("\nDry run enabled, no files were renamed");
            return;
        }

        var tmpMap = new LinkedHashMap<Path, Path>();

        oldNameToNewNameMap.forEach((oldPath, newPath) -> {
            var tmp = oldPath.resolveSibling(UUID.randomUUID() + ".tmp");
            try {
                Files.move(oldPath, tmp, StandardCopyOption.ATOMIC_MOVE);
                tmpMap.put(tmp, newPath);
            } catch (IOException e) {
                throw new RuntimeException("Error moving from original to temporary files", e);
            }
        });

        tmpMap.forEach((tmpPath, newPath) -> {
            try {
                Files.move(tmpPath, newPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                throw new RuntimeException("Error moving from temporary to new files", e);
            }
        });
    }

    /**
     * Checks whether a file name matches the given regular expression pattern.
     *
     * @param file             path to the file
     * @param fileNamePattern  regex pattern to match against the file name (not the full path)
     * @return true if the file name matches the pattern, false otherwise
     */
    public boolean matches(Path file, Pattern fileNamePattern) {
        return fileNamePattern.matcher(file.getFileName().toString()).matches();
    }
}
