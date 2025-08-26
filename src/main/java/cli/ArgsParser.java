package cli;

import model.CliArgs;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ListIterator;

import static java.util.Objects.requireNonNull;

@Component
public class ArgsParser {

    public CliArgs parse(String[] args) {
        if (args.length == 0) {
            help();
            return null;
        }

        var argsAsList = Arrays.asList(args);
        if (argsAsList.contains("--help")) {
            help();
            return null;
        }

        Path dir = null;
        String template = null;

        var iterator = argsAsList.listIterator();
        while (iterator.hasNext()) {
            // prendo l'argomento
            var arg = iterator.next();
            switch (arg) {
                case "--dir" -> dir = Paths.get(requireArg(iterator, "--dir required an argument"));
                case "-t", "--template" -> template = requireArg(iterator, "-t, --template required an argument");
                default -> {
                    System.err.printf("Unknown property %s", arg);
                    help();
                    return null;
                }
            }
        }

        requireNonNull(dir);
        if (!Files.isDirectory(dir))
            throw new  IllegalArgumentException("not a valid path");

        template = template != null ? template.toLowerCase() : null;

        return new CliArgs(dir, template);
    }

    private String requireArg(ListIterator<String> iterator, String errorMessage) {
        if (!iterator.hasNext())
            throw new IllegalArgumentException(errorMessage);
        return iterator.next();
    }

    private void help() {
        System.out.println("""
            Uso:
              java -jar renamer-spring.jar --dir <path/to/dir> --template <olympus_c180>

            Esempi:
              --dir "C:/olympus/120OLYMP" --template olympus_c180
            """);
    }
}
