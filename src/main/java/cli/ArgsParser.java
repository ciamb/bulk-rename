package cli;

import model.CliArgs;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Component
public class ArgsParser {

    public CliArgs parse(String[] args) {
        if (args.length == 0
                || Arrays.asList(args).contains("--help")) {
            printHelp();
            return null;
        }

        Path dir = null;
        String type = null;

        for (int i = 0; i < args.length; i++) {
            String opt = args[i];
            switch (opt) {
                case "--dir" -> dir = Paths.get(requireArg(args, ++i, "--dir richiede un percorso"));
                case "--type" -> type = requireArg(args, ++i, "--type richiede un valore (es. olympus_c180)");
                default -> {
                    System.err.println("Opzione sconosciuta: " + opt);
                    printHelp();
                    return null;
                }
            }
        }

        if (dir == null) throw new IllegalArgumentException("--dir obbligatorio");
        if (type == null) throw new IllegalArgumentException("--type obbligatorio");

        return new CliArgs(dir, type.toLowerCase());
    }

    private String requireArg(String[] args, int idx, String err) {
        if (idx >= args.length) throw new IllegalArgumentException(err);
        return args[idx];
    }

    private void printHelp() {
        System.out.println("""
            Uso:
              java -jar renamer-spring.jar --dir <cartella> --type <png|xls|...> --pi <1..99>

            Esempi:
              --dir "C:/foto" --type png --pi 7
              --dir "/home/ciamb/docs" --type xls --pi 12

            Note:
              - --pi è l'indice primario formattato a 2 cifre (01..99).
              - Il formato del nome è: P1 + <pi_2cifre> + <seq_4cifre> + estensione.
            """);
    }
}
