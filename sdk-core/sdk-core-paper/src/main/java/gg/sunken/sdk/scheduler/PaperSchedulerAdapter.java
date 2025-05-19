package gg.sunken.sdk.scheduler;

import com.google.inject.Inject;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class PaperSchedulerAdapter extends AbstractSchedulerAdapter {

    protected Plugin plugin;
    protected final GlobalSchedulerExecutor globalExecutor;

    @Inject
    public PaperSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.globalExecutor = new GlobalSchedulerExecutor(plugin);
    }

    @Override
    public ScheduledExecutor sync() {
        return globalExecutor;
    }

    @Override
    public ScheduledExecutor entity(Object entity) {
        // Cache this w. caffeine???
        if (entity instanceof Entity bukkitEntity) return new EntitySchedulerExecutor(plugin, bukkitEntity);

        throw new IllegalArgumentException("Entity must be an instance of org.bukkit.entity.Entity");
    }

    @Override
    public ScheduledExecutor block(Object block) {
        // same note above
        if (block instanceof Block bukkitBlock) return new BlockSchedulerExecutor(plugin, bukkitBlock);
        if (block instanceof Location location) return new BlockSchedulerExecutor(plugin, location);
        if (block instanceof Chunk chunk) return new BlockSchedulerExecutor(plugin, chunk.getBlock(0, 0, 0));
        throw new IllegalArgumentException("Block must be an instance of org.bukkit.block.Block, org.bukkit.Location, or org.bukkit.Chunk");
    }
}
