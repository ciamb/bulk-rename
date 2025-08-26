package factory.olympus.c180.jpg;

import factory.Renamer;
import model.Template;
import org.springframework.stereotype.Component;
import utility.RenamerUtility;

import java.nio.file.Path;
import java.util.Map;

import static java.lang.String.format;


@Component
public class OlympusC180JpgRenamer implements Renamer {

    @Override
    public String template() {
        return Template.OLYMPUS_C180.toLowerCase();
    }

    @Override
    public int rename(Path dir) throws Exception {
        validateOlympusC180Dir(dir);

        int folderNumber = RenamerUtility.INSTANCE
            .extractPrefix(dir, Template.OLYMPUS_C180.folderPattern());

        var files = RenamerUtility.INSTANCE
            .listByExtension(dir, Template.OLYMPUS_C180.acceptedExtensions());

        if (files.isEmpty()) {
            System.out.printf("No accepted file found inside dir %s, accepted: %s",
                dir,
                Template.OLYMPUS_C180.acceptedExtensions());
            return 0;
        }

        var sortedFiles = RenamerUtility.INSTANCE.sortFilesByCreationTime(files);

        Map<Path, Path> oldNameToNewNameMap = RenamerUtility.INSTANCE.mapSequence(
            sortedFiles,
            Template.OLYMPUS_C180.seqStart(),
            (seqNum, file) -> "P" + folderNumber + zpad(seqNum) + Template.OLYMPUS_C180.defaultExtension());

        System.out.printf("Preview rename (%d file):", oldNameToNewNameMap.size());
        oldNameToNewNameMap.forEach(
            (oldName, newName) -> System.out.printf(
                "\n%s -> %s",
                oldName.getFileName(),
                newName.getFileName())
        );

        RenamerUtility.INSTANCE.checkConflicts(oldNameToNewNameMap);
        RenamerUtility.INSTANCE.renames(oldNameToNewNameMap);

        System.out.printf("\nRename completed (directory %s).", dir.getFileName());
        return oldNameToNewNameMap.size();
    }

    private void validateOlympusC180Dir(Path dir) {
        var folderName = dir.getFileName().toString();
        var m = Template.OLYMPUS_C180.folderPattern().matcher(folderName);
        if (!m.matches()) {
            throw new IllegalArgumentException("""
                Directory must match Olympus C-180 standard name.
                Ex: NNNOLYMP (NNN >= 100). Esempi: 100OLYMP, 101OLYMP.
                """);
        }
        int num = Integer.parseInt(m.group(1));
        if (num < 100) {
            throw new IllegalArgumentException("Directory number must be >= 100 (got: %d)".formatted(num));
        }
    }

    private String zpad(int value) {
        return format("%0" + Template.OLYMPUS_C180.seqLength() + "d", value);
    }
}

