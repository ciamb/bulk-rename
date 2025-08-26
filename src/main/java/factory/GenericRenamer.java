package factory;

import model.CliArgs;
import model.Template;
import org.springframework.stereotype.Component;
import utility.RenamerUtility;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.lang.String.format;

@Component
public class GenericRenamer implements Renamer {
    @Override
    public String template() {
        return Template.GENERIC.toLowerCase();
    }

    @Override
    public int rename(CliArgs args) throws Exception {
        validateGenericDir(args);

        if (args.prefix() == null || args.prefix().isBlank()) {
            throw new IllegalArgumentException("--name is required for generic renamer");
        }

        var files = RenamerUtility.INSTANCE.listByExtension(
                args.dir(), Template.GENERIC.acceptedExtensions());
        if (files.isEmpty()) {
            System.out.printf("No files found in %s", args.dir());
            return 0;
        }

        var sortedFiles = RenamerUtility.INSTANCE.sortFilesByCreationTime(files);
        int count = sortedFiles.size();
        int padding = Math.max(2, String.valueOf(count).length()); // min 2 -> "01"

        var oldNameToNewNameMap = RenamerUtility.INSTANCE.mapSequence(
                sortedFiles,
                Template.GENERIC.seqStart(),
                (seqNum, file) -> args.prefix() + zpad(seqNum, padding) + getExtension(file)
        );

        if (!cta("\nWith this command " +
                "you're gonna rename all files inside the directory. " +
                "Are you sure? (Y/N): ")) {
            System.out.println("Aborted by user.");
            return 0;
        }

        RenamerUtility.INSTANCE.checkConflicts(oldNameToNewNameMap);
        RenamerUtility.INSTANCE.renames(oldNameToNewNameMap, args.dryRun());

        System.out.printf("\nRename completed (directory %s).", args.dir().getFileName());
        return oldNameToNewNameMap.size();
    }

    private void validateGenericDir(CliArgs args) {
        if (!Files.isDirectory(args.dir())) {
            throw new IllegalArgumentException("Invalid directory: " + args.dir());
        }
    }

    private String getExtension(Path file) {
        var fileName = file.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : Template.GENERIC.defaultExtension();
    }

    private String zpad(int value, int seqLength) {
        return format("%0" + seqLength + "d", value);
    }

    private boolean cta(String prompt) {
        Console c = System.console();
        if (c != null) {
            String ans = c.readLine(prompt);
            return ans != null && ans.trim().equalsIgnoreCase("y");
        } else {
            try {
                System.out.print(prompt);
                byte[] buf = new byte[128];
                int n = System.in.read(buf);
                String ans = n > 0 ? new String(buf, 0, n).trim() : "";
                return ans.equalsIgnoreCase("y");
            } catch (Exception e) {
                return false;
            }
        }
    }
}
