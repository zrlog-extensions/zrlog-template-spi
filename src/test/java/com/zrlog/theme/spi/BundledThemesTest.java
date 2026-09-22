package com.zrlog.theme.spi;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class BundledThemesTest {
    private BundledThemeProvider alpha(boolean fallback) {
        return new BundledThemeProvider() {
            public String id() { return "alpha"; }
            public String engine() { return "freemarker"; }
            public boolean isDefault() { return fallback; }
        };
    }
    @Test public void discoversDefaultAndHonorsDirectoryBoundaries() {
        BundledThemes themes = new BundledThemes(List.of(alpha(true)));
        assertEquals("/include/templates/alpha", themes.defaultPath());
        assertTrue(themes.containsResource("/include/templates/alpha/index.ftl"));
        assertFalse(themes.containsResource("/include/templates/alpha-copy/index.ftl"));
        assertEquals(List.of("include/templates/alpha/index.ftl"), themes.resources());
    }
    @Test public void failsForMissingDefaultWhenRequested() {
        BundledThemes themes = new BundledThemes(List.of(alpha(false)));
        assertThrows(IllegalStateException.class, themes::defaultPath);
    }
    @Test public void rejectsDuplicateProviders() {
        assertThrows(IllegalStateException.class, () -> new BundledThemes(List.of(alpha(true), alpha(false))));
    }
    @Test public void rejectsTraversalAndMissingIndexes() {
        for (String id : List.of("../alpha", "alpha/other", "missing")) {
            assertThrows(RuntimeException.class, () -> new BundledThemes(List.of(new BundledThemeProvider() {
                public String id() { return id; }
                public String engine() { return "freemarker"; }
            })));
        }
    }
}
