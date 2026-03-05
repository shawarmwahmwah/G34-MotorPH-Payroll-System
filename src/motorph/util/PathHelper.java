package motorph.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathHelper {

    private static final String DATA_FOLDER = "data";

    private PathHelper() {}

    public static Path getDataFile(String filename) {
        return Paths.get(DATA_FOLDER, filename);
    }
}