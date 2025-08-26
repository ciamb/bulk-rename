package cli;

import model.CliArgs;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ListIterator;

import static java.util.Objects.requireNonNull;
import static model.Template.GENERIC;

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
        boolean dryRun = false;
        String prefix = null;

        var iterator = argsAsList.listIterator();
        while (iterator.hasNext()) {
            // prendo l'argomento
            var arg = iterator.next();
            switch (arg) {
                case "--dir" -> dir = Paths.get(requireArg(iterator, "--dir required a valid argument"));
                case "-t", "--template" -> template = requireArg(iterator, "-t, --template required an argument (ex. generic, olympus_c180)");
                case "--dry-run" -> dryRun = true;
                case "--prefix" ->  prefix = requireArg(iterator, "--prefix required an argument (ex. IMG_");
                default -> {
                    System.err.printf("Unknown property %s", arg);
                    help();
                    return null;
                }
            }
        }

        requireNonNull(dir);
        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Path not valid");
        }

        template = template != null ? template.toLowerCase() : null;

        if (GENERIC.toLowerCase().equalsIgnoreCase(template)
                && (prefix == null || prefix.isBlank())) {
            throw new IllegalArgumentException(
                "--prefix is required when template is --template=generic");
        }

        return new CliArgs(dir, template, dryRun, prefix);
    }

    private String requireArg(ListIterator<String> iterator, String errorMessage) {
        if (!iterator.hasNext())
            throw new IllegalArgumentException(errorMessage);
        return iterator.next();
    }

    private void help() {
        System.out.println("""
            Usage:
              java -jar renamer-spring.jar --dir <path/to/dir> --template <olympus_c180>

            Example:
              --dir "C:/olympus/120OLYMP" --template olympus_c180 -> P120XXXX.JPG
              --dir "C:/files" -t generic --prefix elmt_ -> elmt_XXX.*
            """);
    }
}
