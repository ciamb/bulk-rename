package app;

import cli.ArgsParser;
import factory.RenamerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"app", "cli", "factory", "model", "utility"})
public class BulkRenamer implements CommandLineRunner {

    private final ArgsParser argsParser;
    private final RenamerFactory renamerFactory;

    public BulkRenamer(
            ArgsParser argsParser, RenamerFactory renamerFactory) {
        this.argsParser = argsParser;
        this.renamerFactory = renamerFactory;
    }

    public static void main(String[] args) {
        SpringApplication.run(BulkRenamer.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        var cliArgs = argsParser.parse(args);
        if (cliArgs == null) return;

        var count = renamerFactory
                .get(cliArgs.template())
                .rename(cliArgs.dir());
        System.out.printf("\nRinominati %d file.", count);
    }
}
