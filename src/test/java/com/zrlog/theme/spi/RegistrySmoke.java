package com.zrlog.theme.spi;
/** Runs with actual provider JARs on both JVM and Native Image. */
public final class RegistrySmoke {
    public static void main(String[] args) throws Exception {
        BundledThemes registry = BundledThemes.getInstance();
        if (registry.providers().size() != Integer.parseInt(args[0])) throw new AssertionError(registry.paths());
        if (!registry.contains(registry.defaultPath())) throw new AssertionError("Missing default");
        long bytes = 0;
        for (String resource : registry.resources()) {
            try (java.io.InputStream in = RegistrySmoke.class.getClassLoader().getResourceAsStream(resource)) {
                if (in == null) throw new AssertionError(resource);
                bytes += in.readAllBytes().length;
            }
        }
        System.out.println("Discovered " + registry.paths() + "; resources=" + registry.resources().size() + "; bytes=" + bytes);
    }
}
