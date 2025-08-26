package model;

import java.util.List;
import java.util.regex.Pattern;

public enum Template {
    GENERIC(
            1,
            1,
            "",
            List.of(""),
            null
    ),
    OLYMPUS_C180(
            1,
            4,
            ".JPG",
            List.of(".jpg"),
            Pattern.compile("^([1-9]\\d{2,})OLYMP$", Pattern.CASE_INSENSITIVE));

    private final int seqStart;
    private final int seqLength;
    private final String defaultExtension;
    private final List<String> acceptedExtensions;
    private final Pattern folderPattern;

    Template(int seqStart,
             int seqLength,
             String defaultExtension,
             List<String> acceptedExtensions,
             Pattern folderPattern) {
        this.seqStart = seqStart;
        this.seqLength = seqLength;
        this.defaultExtension = defaultExtension;
        this.acceptedExtensions = acceptedExtensions;
        this.folderPattern = folderPattern;
    }

    public int seqStart() {
        return seqStart;
    }

    public int seqLength() {
        return seqLength;
    }

    public String defaultExtension() {
        return defaultExtension;
    }

    public List<String> acceptedExtensions() {
        return acceptedExtensions;
    }

    public Pattern folderPattern() {
        return folderPattern;
    }

    public String toLowerCase() {
        return name().toLowerCase();
    }
}
