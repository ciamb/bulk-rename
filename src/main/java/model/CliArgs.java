package model;

import java.nio.file.Path;

public record CliArgs(
        Path dir,
        String fileType // optional
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Path dir;
        private String fileType;

        public Builder dir(Path dir) {
            this.dir = dir;
            return this;
        }

        public Builder fileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public CliArgs build() {
            return new CliArgs(dir, fileType);
        }
    }

}
