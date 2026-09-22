package com.zrlog.theme.spi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/** Immutable registry. Duplicate identifiers and conflicting defaults are configuration errors. */
public final class BundledThemes {
    private final Map<String, BundledThemeProvider> providers;
    private final List<String> resources;
    private final String defaultPath;

    private static final class Holder {
        private static final BundledThemes INSTANCE = load(BundledThemeProvider.class.getClassLoader());
    }

    public static BundledThemes getInstance() { return Holder.INSTANCE; }

    public static BundledThemes load(ClassLoader loader) {
        List<BundledThemeProvider> providers = new ArrayList<>();
        ServiceLoader.load(BundledThemeProvider.class, loader).forEach(providers::add);
        return new BundledThemes(providers);
    }

    public BundledThemes(Collection<? extends BundledThemeProvider> discovered) {
        Map<String, BundledThemeProvider> found = new TreeMap<>();
        Set<String> indexed = new TreeSet<>();
        String fallback = null;
        for (BundledThemeProvider provider : discovered) {
            String id = provider.id();
            if (id == null || !id.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
                throw new IllegalArgumentException("Invalid bundled theme id: " + id);
            }
            String path = "/include/templates/" + id;
            if (!path.equals(provider.path()) || !provider.resourceIndex().equals("META-INF/zrlog/themes/" + id + ".resources")) {
                throw new IllegalArgumentException("Invalid bundled theme paths: " + id);
            }
            if (provider.engine() == null || provider.engine().isBlank()) {
                throw new IllegalArgumentException("Missing bundled theme engine: " + id);
            }
            if (found.putIfAbsent(path, provider) != null) {
                throw new IllegalStateException("Duplicate bundled theme: " + id);
            }
            if (provider.isDefault()) {
                if (fallback != null) throw new IllegalStateException("Multiple default bundled themes: " + fallback + ", " + path);
                fallback = path;
            }
            ClassLoader loader = provider.getClass().getClassLoader();
            try (InputStream input = loader.getResourceAsStream(provider.resourceIndex())) {
                if (input == null) throw new IllegalStateException("Missing resource index for theme: " + id);
                List<String> entries = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                        .lines().filter(line -> !line.isBlank()).collect(Collectors.toList());
                if (entries.isEmpty()) throw new IllegalStateException("Empty resource index for theme: " + id);
                for (String entry : entries) {
                    if (!entry.startsWith(path.substring(1) + "/") || entry.contains("..") || entry.contains("\\") || loader.getResource(entry) == null) {
                        throw new IllegalStateException("Invalid or missing theme resource: " + entry);
                    }
                    indexed.add(entry);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Unable to read theme index: " + id, e);
            }
        }
        providers = Collections.unmodifiableMap(found);
        resources = List.copyOf(indexed);
        defaultPath = fallback;
    }

    public Collection<BundledThemeProvider> providers() { return providers.values(); }
    public List<String> paths() { return List.copyOf(providers.keySet()); }
    public List<String> resources() { return resources; }
    public Optional<BundledThemeProvider> find(String path) { return Optional.ofNullable(providers.get(path)); }
    public boolean contains(String path) { return providers.containsKey(path); }
    public boolean containsResource(String path) {
        return path != null && paths().stream().anyMatch(root -> path.equals(root) || path.startsWith(root + "/"));
    }
    public String defaultPath() {
        if (defaultPath == null) throw new IllegalStateException("No default bundled theme provider on the application classpath");
        return defaultPath;
    }
}
