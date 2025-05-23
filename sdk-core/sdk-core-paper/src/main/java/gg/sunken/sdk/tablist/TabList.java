package gg.sunken.sdk.tablist;

import gg.sunken.sdk.tablist.api.GameFeature;
import gg.sunken.sdk.tablist.api.PlatformProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

/**
 * Represents a tab list.
 *
 * <p>Implementations of this class handle the update logic for different versions of the game.</p>
 *
 * @see GameFeature
 * @see TabListTemplate
 */
public abstract class TabList {
    /**
     * The player's uuid.
     */
    protected final UUID uuid;
    /**
     * The {@link TabListTemplate tab list template}.
     */
    protected final TabListTemplate template;

    /**
     * The {@link PlatformProvider platform provider}.
     */
    protected final PlatformProvider platformProvider;

    /**
     * The player's {@link GameFeature supported features}.
     */
    protected final Set<GameFeature> gameFeatures;

    /**
     * Creates a new tab list provider.
     *
     * @param uuid             the player
     * @param template         the template
     */
    protected TabList(@NotNull UUID uuid, @NotNull PlatformProvider platformProvider, @NotNull TabListTemplate template) {
        this.uuid = uuid;
        this.platformProvider = platformProvider;
        this.template = template;
        this.gameFeatures = platformProvider.supportedFeatures(uuid);
    }

    /**
     * Updates the tab list. It gets called upon every tick.
     */
    public abstract void update();

    /**
     * Destroys the tab list. It restores the previous state of the tab list seen by the player.
     */
    protected abstract void destroy();

    /**
     * Returns the {@link TabListTemplate tab list template} used by this {@link TabList provider}.
     *
     * @param <T> the type of this template
     * @return the {@link TabListTemplate tab list template}
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public final <T extends TabListTemplate> T template() {
        return (T) this.template;
    }
}