package gg.sunken.sdk.tablist.impl;


import gg.sunken.sdk.tablist.TabList;
import gg.sunken.sdk.tablist.TabListEntry;
import gg.sunken.sdk.tablist.TabListHeaderAndFooter;
import gg.sunken.sdk.tablist.TabListTemplate;
import gg.sunken.sdk.tablist.api.PlatformProvider;
import gg.sunken.sdk.tablist.api.UpdateAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A tab list provider for versions 1.8 - latest.
 *
 * @see TabList
 */
public final class ModernTabList extends TabList {
    private final List<Object> skinChanged;
    private final List<Object> textChanged;
    private final List<Object> pingChanged;

    /**
     * Creates a provider instance.
     *
     * @param uuid             the player
     * @param template         the template
     */
    public ModernTabList(@NotNull UUID uuid, @NotNull PlatformProvider platformProvider, @NotNull TabListTemplate template) {
        super(uuid, platformProvider, template);
        this.textChanged = new ArrayList<>();
        this.skinChanged = new ArrayList<>();
        this.pingChanged = new ArrayList<>();
    }

    @Override
    public void update() {
        updateTabList();
        updateHeaderFooter();
    }

    @Override
    protected void destroy() {
        List<Object> baked = new ArrayList<>();

        for (TabListEntry entry : this.template.entries()) {
            baked.add(this.platformProvider.bakeEntry(entry, this.gameFeatures));
        }

        clearBakedData();
        this.platformProvider.send(this.uuid, this.gameFeatures, UpdateAction.REMOVE, baked);
    }

    private void updateTabList() {
        this.template.update();

        for (int i = 0; i < TabListTemplate.TEMPLATE_MAX_SIZE; i++) {
            TabListEntry entry = this.template.entries()[i];
            entry.update();

            // If the entry has nothing in it, fall back to the default entry provider
            if (!entry.dirty() && entry.entryProvider() == null) {
                entry.setEntryProvider(this.template.defaultProvider());
                entry.update();
            }

            // We check if anything changed
            if (entry.anyChanged()) {
                Object baked = this.platformProvider.bakeEntry(entry, this.gameFeatures);

                if (entry.skinChanged()) {
                    this.skinChanged.add(baked);
                } else {
                    if (entry.textChanged()) {
                        this.textChanged.add(baked);
                    }

                    if (entry.pingChanged()) {
                        this.pingChanged.add(baked);
                    }
                }
            }

            if (this.template.updateEntryStates()) {
                entry.resetState();
            }
        }

        this.platformProvider.send(this.uuid, this.gameFeatures, UpdateAction.ADD, this.skinChanged);
        this.platformProvider.send(this.uuid, this.gameFeatures, UpdateAction.UPDATE_NAME, this.textChanged);
        this.platformProvider.send(this.uuid, this.gameFeatures, UpdateAction.UPDATE_LATENCY, this.pingChanged);
        clearBakedData();
    }

    private void updateHeaderFooter() {
        TabListHeaderAndFooter entry = this.template.headerAndFooter();
        entry.update();

        // We check if anything changed
        if (entry.anyChanged()) {
            this.platformProvider.sendHeaderAndFooter(this.uuid, entry.newHeader(), entry.newFooter());
        }

        if (this.template.updateEntryStates()) {
            entry.resetState();
        }
    }

    private void clearBakedData() {
        this.skinChanged.clear();
        this.textChanged.clear();
        this.pingChanged.clear();
    }
}