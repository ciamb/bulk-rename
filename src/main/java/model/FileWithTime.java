package model;

import java.nio.file.Path;
import java.time.Instant;

public record FileWithTime(
        Path path,
        Instant time
) {
}
