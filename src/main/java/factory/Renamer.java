package factory;

import model.CliArgs;

import java.nio.file.Path;

public interface Renamer {
    String template();
    int rename(CliArgs args) throws Exception;
}
