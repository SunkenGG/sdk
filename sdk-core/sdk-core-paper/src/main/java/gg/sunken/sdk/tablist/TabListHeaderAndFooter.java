package gg.sunken.sdk.tablist;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * An object holding header and footer related data.
 *
 * @see TabListTemplate
 */
@ApiStatus.Internal
public final class TabListHeaderAndFooter {
    private Component currentHeader;
    private Component newHeader;

    private Component currentFooter;
    private Component newFooter;

    private boolean headerChanged;
    private boolean footerChanged;

    TabListHeaderAndFooter() {
        // empty
    }

    /**
     * Updates all the values of this {@link TabListHeaderAndFooter object}.
     */
    public void update() {
        // Checking for header changes
        {
            this.headerChanged = (this.currentHeader == null && this.newHeader != null) ||
                (this.currentHeader != null && !this.currentHeader.equals(this.newHeader));
            this.currentHeader = this.newHeader;
        }

        // Checking for footer changes
        {
            this.footerChanged = (this.currentFooter == null && this.newFooter != null) ||
                (this.currentFooter != null && !this.currentFooter.equals(this.newFooter));
            this.currentFooter = this.newFooter;
        }
    }

    /**
     * Resets the {@link TabListHeaderAndFooter object}'s mutation state.
     */
    public void resetState() {
        this.newHeader = null;
        this.newFooter = null;
    }

    /**
     * Returns if any change happened inside this {@link TabListHeaderAndFooter object}.
     *
     * @return {@code true} if any of the values changed, {@code false} otherwise
     */
    public boolean anyChanged() {
        return this.headerChanged || this.footerChanged;
    }

    /**
     * Sets the new {@link Component header} of this {@link TabListHeaderAndFooter object}.
     *
     * @param header the new {@link Component header}
     */
    void setNewHeader(@Nullable Component header) {
        this.newHeader = header;
    }

    /**
     * Sets the new {@link Component footer} of this {@link TabListHeaderAndFooter object}.
     *
     * @param footer the new {@link Component footer}
     */
    void setNewFooter(@Nullable Component footer) {
        this.newFooter = footer;
    }

    /**
     * Returns the {@link Component current header} of this {@link TabListHeaderAndFooter object}.
     *
     * @return the {@link Component current header}
     */
    @UnknownNullability
    Component currentHeader() {
        return this.currentHeader;
    }

    /**
     * Returns the {@link Component new header} of this {@link TabListHeaderAndFooter object}.
     *
     * @return the {@link Component new header}
     */
    @UnknownNullability
    public Component newHeader() {
        return this.newHeader;
    }

    /**
     * Returns the {@link Component current footer} of this {@link TabListHeaderAndFooter object}.
     *
     * @return the {@link Component current footer}
     */
    @UnknownNullability
    Component currentFooter() {
        return this.currentFooter;
    }

    /**
     * Returns the {@link Component new footer} of this {@link TabListHeaderAndFooter object}.
     *
     * @return the {@link Component new footer}
     */
    @UnknownNullability
    public Component newFooter() {
        return this.newFooter;
    }
}