package model;

import java.nio.file.Path;

public record CliArgs(
        Path dir,
        String template, // optional
        boolean dryRun
) {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Path dir;
        private String template;
        private boolean dryRun = false;

        public Builder dir(Path dir) {
            this.dir = dir;
            return this;
        }

        public Builder template(String template) {
            this.template = template;
            return this;
        }

        public Builder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public CliArgs build() {
            return new CliArgs(dir, template, dryRun);
        }
    }
}
