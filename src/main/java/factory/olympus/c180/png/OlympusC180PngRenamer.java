package factory.olympus.c180.png;

import factory.Renamer;
import model.FileType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class OlympusC180PngRenamer implements Renamer {
    private static final Pattern FOLDER_PATTERN = Pattern.compile("^([1-9]\\d{2,})OLYMP$", Pattern.CASE_INSENSITIVE);
    private static final int SEQ_LEN = 4; // 0001..9999

    @Override
    public String type() {
        return FileType.OLYMPUS_C180.toLowerCase();
    }

    @Override
    public int rename(Path dir) throws Exception {
        validateDirName(dir);

        int folderNumber = extractFolderNumber(dir);
        var files = listJpg(dir);

        if (files.isEmpty()) {
            System.out.println("Nessun .png trovato in " + dir);
            return 0;
        }

        files = sortByTime(files);

        // Build mapping: P<folderNumber><seq4>.png  con seq da 0001
        var mappings = new LinkedHashMap<Path, Path>();
        int seq = 1;
        for (var src : files) {
            String seqStr = ("%0" + SEQ_LEN + "d").formatted(seq++);
            String newName = "P" + folderNumber + seqStr + ".png";
            mappings.put(src, src.resolveSibling(newName));
        }

        checkConflicts(mappings);

        System.out.println("Anteprima rinomina (" + mappings.size() + " file):");
        mappings.forEach((s, t) -> System.out.println("  " + s.getFileName() + " -> " + t.getFileName()));

        // Rinominazione sicura in due fasi
        Map<Path, Path> temp = new LinkedHashMap<>();
        for (var e : mappings.entrySet()) {
            Path src = e.getKey();
            Path tmp = src.resolveSibling(UUID.randomUUID() + ".tmp");
            Files.move(src, tmp, StandardCopyOption.ATOMIC_MOVE);
            temp.put(tmp, e.getValue());
        }
        for (var e : temp.entrySet()) {
            Files.move(e.getKey(), e.getValue(), StandardCopyOption.ATOMIC_MOVE);
        }

        System.out.println("Rinomina completata (cartella " + dir.getFileName() + ").");
        return mappings.size();
    }


    private void validateDirName(Path dir) {
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Percorso non valido: " + dir);
        }
        String folderName = dir.getFileName().toString();
        Matcher m = OlympusC180PngRenamer.FOLDER_PATTERN.matcher(folderName);
        if (!m.matches()) {
            throw new IllegalArgumentException("La cartella deve chiamarsi NNNOLYMP (NNN >= 100). Esempi: 100OLYMP, 101OLYMP.");
        }
        int num = Integer.parseInt(m.group(1));
        if (num < 100) {
            throw new IllegalArgumentException("Numero cartella deve essere >= 100 (trovato: " + num + ").");
        }
    }

    private int extractFolderNumber(Path dir) {
        String name = dir.getFileName().toString();
        Matcher m = FOLDER_PATTERN.matcher(name);
        if (!m.matches()) throw new IllegalStateException("Nome cartella non valido: " + name);
        return Integer.parseInt(m.group(1));
    }

    private List<Path> listJpg(Path dir) throws IOException {
        try (var s = Files.list(dir)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jpg"))
                    .toList();
        }
    }

    private List<Path> sortByTime(List<Path> files) {
        return files.stream()
                .map(p -> new Timed(p, bestTime(p)))
                .sorted(Comparator
                        .comparing(Timed::time)
                        .thenComparing(t -> t.path().getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                .map(Timed::path)
                .toList();
    }

    private Instant bestTime(Path p) {
        try {
            var attrs = Files.readAttributes(p, BasicFileAttributes.class);
            var ct = attrs.creationTime();
            return (ct != null ? ct.toInstant() : attrs.lastModifiedTime().toInstant());
        } catch (IOException e) {
            try {
                return Files.getLastModifiedTime(p).toInstant();
            } catch (IOException ex) {
                return Instant.EPOCH;
            }
        }
    }

    private void checkConflicts(Map<Path, Path> map) throws IOException {
        var seen = new HashSet<String>();
        for (var target : map.values()) {
            var name = target.getFileName().toString();
            if (!seen.add(name)) {
                throw new IllegalStateException("Conflitto: target duplicato " + name);
            }
        }
        for (var e : map.entrySet()) {
            Path src = e.getKey();
            Path tgt = e.getValue();
            if (Files.exists(tgt) && !Files.isSameFile(src, tgt)) {
                throw new IllegalStateException("Conflitto: esiste già " + tgt.getFileName() + " in cartella.");
            }
        }
    }

    private record Timed(Path path, Instant time) {
    }
}

