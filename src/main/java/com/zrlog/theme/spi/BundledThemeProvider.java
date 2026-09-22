package com.zrlog.theme.spi;

/** ServiceLoader contract implemented by independently versioned theme resource JARs. */
public interface BundledThemeProvider {
    /** Stable theme identifier, also the directory below include/templates/. */
    String id();
    /** Rendering engine identifier, for example freemarker or hexo. */
    String engine();
    /** Exactly one bundled provider should be the application fallback. */
    default boolean isDefault() { return false; }
    /** Optional renderer compatibility adapter; never inferred from a directory name. */
    default String adapter() { return ""; }
    default String path() { return "/include/templates/" + id(); }
    default String resourceIndex() { return "META-INF/zrlog/themes/" + id() + ".resources"; }
}
