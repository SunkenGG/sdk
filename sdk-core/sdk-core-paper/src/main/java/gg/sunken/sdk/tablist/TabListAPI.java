package gg.sunken.sdk.tablist;

import gg.sunken.sdk.tablist.api.PlatformProvider;
import gg.sunken.sdk.tablist.api.TabListFactory;
import gg.sunken.sdk.tablist.impl.ModernTabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * The wrapper for managing tab lists.
 *
 * <p>This class is a utility for creating {@link TabList tab lists} for players.</p>
 *
 * <p>Every developer must track the provided tab lists for themselves.</p>
 *
 * @see TabListTemplate
 * @see TabList
 */
public class TabListAPI {
    /**
     * The default provider factory instance supplying {@link ModernTabList}.
     */
    private static final TabListFactory DEFAULT_FACTORY = ModernTabList::new;

    private final PlatformProvider platformProvider;
    private final TabListFactory factory;

    /**
     * Creates a new TabList instance.
     *
     * @param platformProvider the platform provider
     */
    public TabListAPI(@NotNull PlatformProvider platformProvider) {
        this(platformProvider, DEFAULT_FACTORY);
    }

    /**
     * Creates a new TabList instance.
     *
     * @param platformProvider the platform provider
     * @param factory          the provider factory
     */
    public TabListAPI(@NotNull PlatformProvider platformProvider, @NotNull TabListFactory factory) {
        this.platformProvider = platformProvider;
        this.factory = factory;
    }

    /**
     * Attempts to provide a {@link TabList tab list} for the given player.
     *
     * @param uuid     the player
     * @param template the template
     * @return a new {@link TabList tab list}, or {@code null} if the factory
     * failed to create a tab list
     */
    @Nullable
    public final TabList provideTabList(@NotNull UUID uuid, @NotNull TabListTemplate template) {
        return this.factory.provideTabList(uuid, this.platformProvider, template);
    }
}