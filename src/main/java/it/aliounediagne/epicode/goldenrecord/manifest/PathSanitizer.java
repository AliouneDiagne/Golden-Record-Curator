package it.aliounediagne.epicode.goldenrecord.manifest;

import it.aliounediagne.epicode.goldenrecord.exceptions.InsecurePathException;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Difesa contro path traversal: il percorso viene risolto nella cartella base
 * e verificato dopo normalize().
 */
public class PathSanitizer {

    private final Path baseDir;


    public PathSanitizer(Path baseDir) {
        this.baseDir = baseDir.toAbsolutePath().normalize();
    }


    public Path sanitize(Path requested) {
        Path resolved = baseDir.resolve(requested)
                .normalize()
                .toAbsolutePath();

        if (!resolved.startsWith(baseDir)) {
            throw new InsecurePathException(requested.toString(), resolved.toString());
        }
        return resolved;
    }

    public Path sanitize(String fileName) {
        return sanitize(Paths.get(fileName));
    }
}
