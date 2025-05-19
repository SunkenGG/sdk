package gg.sunken.sdk.tablist.api;

import gg.sunken.sdk.tablist.TabList;
import gg.sunken.sdk.tablist.TabListTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A simple interface for creating {@link TabList tab list} instances.
 */
@FunctionalInterface
public interface TabListFactory {
    /**
     * Attempts to provide a {@link TabList tab list} for the given player.
     *
     * @param uuid             the player
     * @param template         the template
     * @return a new {@link TabList tab list}, or {@code null} if the factory
     * failed to create a tab list
     */
    @Nullable
    TabList provideTabList(
        @NotNull UUID uuid,
        @NotNull PlatformProvider platformProvider,
        @NotNull TabListTemplate template
    );
}