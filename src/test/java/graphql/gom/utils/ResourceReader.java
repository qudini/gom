package graphql.gom.utils;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class ResourceReader {

    public static String readResource(String resource) {
        try {
            return new String(
                    Files.readAllBytes(Paths.get(
                            ResourceReader
                                    .class
                                    .getResource(resource)
                                    .toURI()
                    )),
                    UTF_8
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

}
