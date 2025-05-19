package gg.sunken.sdk.plugin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import gg.sunken.sdk.guice.GuiceSPI;
import gg.sunken.sdk.guice.PaperSDKModule;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Slf4j
@SuppressWarnings("UnstableApiUsage")
public class SdkPluginBootstrap implements PluginBootstrap {

    private Injector injector;
    private Class<? extends JavaPlugin> mainClass;
    private File dataFolder;

    @SuppressWarnings("unchecked") // - checked by underlying impl
    @Override
    public void bootstrap(BootstrapContext context) {
        try {
            this.dataFolder = context.getDataDirectory().toFile();
            this.mainClass = (Class<? extends JavaPlugin>) Class.forName(context.getConfiguration().getMainClass());
            
            this.injector = Guice.createInjector(
                new PaperSDKModule(this.mainClass),
                GuiceSPI.discoverModules(Module.class, this.getClass().getClassLoader())
            );
        } catch (ClassNotFoundException e) {
            log.error("Failed to load main class", e);
        }
    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        // Inject here to have everything in the plugin injectable
        return (JavaPlugin) this.injector.getInstance(Plugin.class);
    }
    
}
