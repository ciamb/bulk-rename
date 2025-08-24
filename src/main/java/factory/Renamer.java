package factory;

import java.nio.file.Path;

public interface Renamer {
    String type();
    int rename(Path dir) throws Exception;
}
