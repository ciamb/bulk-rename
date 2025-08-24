package factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RenamerFactory {
    private final Map<String, Renamer> renamers;

    public RenamerFactory(List<Renamer> renamers) {
        this.renamers = renamers.stream()
            .collect(Collectors.toUnmodifiableMap(
        r -> r.type().toLowerCase(),
        r -> r
            ));
    }

    public Renamer get(String fileType) {
        var renamer = renamers.get(fileType.toLowerCase());
        return Optional.ofNullable(renamer)
            .orElseThrow(() -> new IllegalArgumentException(
                "type %s not supported, aviable: %s"
                .formatted(fileType, renamers.keySet())));
    }

}
