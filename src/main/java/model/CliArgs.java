package model;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

public record CliArgs(
        Path dir,
        String template, // optional
        boolean dryRun,
        String prefix
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Path dir;
        private String template;
        private boolean dryRun = false;
        private String prefix;

        public Builder dir(Path dir) {
            this.dir = requireNonNull(dir);
            return this;
        }

        public Builder template(String template) {
            this.template = requireNonNull(template).toLowerCase();
            return this;
        }

        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public Builder prefix(String prefix) {
            this.prefix = requireNonNull(prefix);
            return this;
        }

        public CliArgs build() {
            return new CliArgs(dir, template, dryRun, prefix);
        }
    }
}
