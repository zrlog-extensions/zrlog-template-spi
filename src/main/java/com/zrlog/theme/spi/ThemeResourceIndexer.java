package com.zrlog.theme.spi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Build-time resource inventory and GraalVM metadata generator. No filesystem scanning at runtime. */
public final class ThemeResourceIndexer {
    private ThemeResourceIndexer() { }
    public static void main(String[] args) throws IOException {
        if (args.length != 4) throw new IllegalArgumentException("Expected resource root, output directory, theme id and provider class");
        Path source = Path.of(args[0]);
        Path output = Path.of(args[1]);
        String id = args[2];
        String provider = args[3];
        if (!id.matches("[a-z0-9]+(?:-[a-z0-9]+)*") || !provider.matches("[a-zA-Z_$][a-zA-Z0-9_$.]*")) {
            throw new IllegalArgumentException("Invalid theme id or provider class");
        }
        String prefix = "include/templates/" + id;
        List<String> files;
        try (Stream<Path> paths = Files.walk(source.resolve(prefix))) {
            files = paths.filter(Files::isRegularFile).map(source::relativize)
                    .map(path -> path.toString().replace('\\', '/')).sorted().collect(Collectors.toList());
        }
        if (files.isEmpty()) throw new IllegalArgumentException("Theme has no resources: " + id);
        write(output.resolve("META-INF/zrlog/themes/" + id + ".resources"), String.join("\n", files) + "\n");
        Path nativeDir = output.resolve("META-INF/native-image/com.hibegin/zrlog-template-" + id);
        write(nativeDir.resolve("resource-config.json"), "{\"resources\":{\"includes\":[{\"pattern\":\"" + prefix + "/.*\"},{\"pattern\":\"META-INF/zrlog/themes/" + id + "[.]resources\"},{\"pattern\":\"META-INF/services/com[.]zrlog[.]theme[.]spi[.]BundledThemeProvider\"}]}}\n");
        write(nativeDir.resolve("reflect-config.json"), "[{\"name\":\"" + provider + "\",\"allPublicConstructors\":true}]\n");
    }
    private static void write(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, text, StandardCharsets.UTF_8);
    }
}
