package gg.sunken.sdk.plugin;

import com.google.inject.Injector;
import gg.sunken.sdk.command.CommandRegistry;
import gg.sunken.sdk.features.Feature;
import gg.sunken.sdk.features.PaperFeature;
import gg.sunken.sdk.features.PaperFeatureManager;
import gg.sunken.sdk.features.PaperFeatureTicker;
import gg.sunken.sdk.guice.GuiceServiceLoader;
import gg.sunken.sdk.scheduler.SchedulerAdapter;
import gg.sunken.sdk.stringtoitem.ItemSerializer;
import gg.sunken.sdk.stringtoitem.StringToItem;
import gg.sunken.sdk.utils.ArmorstandUtils;
import gg.sunken.sdk.utils.ReflectionUtil;
import gg.sunken.sdk.utils.ServerLock;
import io.papermc.paper.plugin.configuration.PluginMeta;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;

import java.util.logging.Level;

/**
 * A generic class on top of {@link JavaPlugin} that hooks into some of the WildWood SDK
 * to provide a more streamlined development experience.
 *
 * @author santio, casper
 */
@SuppressWarnings({"WeakerAccess"})
@Getter
@Accessors(fluent = true)
public abstract class SdkPaperPlugin extends JavaPlugin {

    private final PaperFeatureManager featureManager;
    private final SchedulerAdapter schedulerAdapter;
    private final Injector injector;
    
    private @MonotonicNonNull CommandRegistry commandRegistry;

    public SdkPaperPlugin(
            PaperFeatureManager featureManager,
            SchedulerAdapter schedulerAdapter,
            Injector injector
    ) {
        this.featureManager = featureManager;
        this.schedulerAdapter = schedulerAdapter;
        this.injector = injector;

    }

    /**
     * Dispatched before the {@link #onLoad()} sequence.
     */
    @ApiStatus.OverrideOnly
    public void preLoad() {
    }

    /**
     * Dispatched after the {@link #onLoad()} sequence.
     */
    @ApiStatus.OverrideOnly
    public void load() {
    }

    /**
     * Dispatched before the {@link #onEnable()} sequence.
     */
    @ApiStatus.OverrideOnly
    public void preEnable() {
    }

    /**
     * Dispatched after the {@link #onEnable()} sequence.
     */
    @ApiStatus.OverrideOnly
    public abstract void enable();

    /**
     * Dispatched before the {@link #onDisable()} sequence.
     */
    @ApiStatus.OverrideOnly
    public void preDisable() {
    }

    /**
     * Dispatched after the {@link #onDisable()} sequence.
     */
    @ApiStatus.OverrideOnly
    public void disable() {
    }

    /**
     * Sets the name of the plugin.
     *
     * @param name The new name of the plugin.
     */
    public void setName(String name) {
        try {
            PluginMeta meta = getPluginMeta();

            if (meta.getName().equals(name)) return;

            if (getServer().getPluginManager().getPlugin(name) != null) {
                getLogger().log(Level.WARNING, "Plugin with name " + name + " already exists!");
                return;
            }

            if (!name.matches("[a-zA-Z0-9_\\-.]+")) {
                getLogger().log(Level.WARNING, "Invalid name for plugin! Name must be a-z,A-Z,0-9,_,.,-");
                return;
            }

            ReflectionUtil.setPrivateField(meta, "name", name);
            Class.forName("", false, getClassLoader());

            return;
        } catch (Exception ignored) {
        }

        PluginDescriptionFile descriptionFile = getDescription();
        if (descriptionFile.getName().equals(name)) return;

        if (getServer().getPluginManager().getPlugin(name) != null) {
            getLogger().log(Level.WARNING, "Plugin with name " + name + " already exists!");
            return;
        }
        if (!name.matches("[a-zA-Z0-9_\\-.]+")) {
            getLogger().log(Level.WARNING, "Invalid name for plugin! Name must be a-z,A-Z,0-9,_,.,-");
            return;
        }
        ReflectionUtil.setPrivateField(descriptionFile, "name", name);
    }

    @Override
    public final void onLoad() {
        this.preLoad();
        
        this.featureManager.loadAll();
        this.featureManager.call(PaperFeature::load);
        
        this.load();
    }

    @Override
    public final void onEnable() {
        this.preEnable();

        this.commandRegistry = new CommandRegistry(this);
        commandRegistry.registerCommands(this.getClass().getClassLoader());

        GuiceServiceLoader.load(Listener.class)
                .forEach(this::registerListener);

        GuiceServiceLoader.load(ItemSerializer.class)
                .forEach(this::registerItemSerializers);

        new ServerLock(this);
        new ArmorstandUtils(this);
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(
            this,
            new PaperFeatureTicker(this.featureManager),
            1L,
            1L
        );
        
        this.featureManager.call(Feature::enable);
        this.enable();
    }

    @Override
    public final void onDisable() {
        this.preDisable();
        
        this.featureManager.call(Feature::disable);
        commandRegistry.unregisterCommands();

        this.disable();
    }

    private void registerListener(Class<? extends Listener> listener) {
        final Listener listenerInstance = this.injector.getInstance(listener);
        this.getServer().getPluginManager().registerEvents(listenerInstance, this);
    }

    private void registerItemSerializers(Class<? extends ItemSerializer> itemSerializer) {
        final ItemSerializer serializerInstance = this.injector.getInstance(itemSerializer);
        StringToItem.register(serializerInstance);
    }

}