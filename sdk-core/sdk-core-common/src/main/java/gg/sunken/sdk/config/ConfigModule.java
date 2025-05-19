package gg.sunken.sdk.config;

import com.google.auto.service.AutoService;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import java.io.File;
import java.nio.file.Files;

@AutoService(Module.class)
public class ConfigModule extends AbstractModule {
    private final ConfigProvider provider;

    public ConfigModule(File dataFolder) {
        this.provider = new ConfigProvider(dataFolder);

        dataFolder.mkdirs();
        dataFolder.toPath().iterator().forEachRemaining(path -> {
            if (Files.isDirectory(path)) {
                File[] files = path.toFile().listFiles((dir, name) -> name.endsWith(".yml"));
                if (files != null) {
                    for (File file : files) {
                        String name = file.getName().replace(".yml", "");
                        provider.loadConfig(name, file);
                    }
                }
            }
        });
    }

    @Override
    protected void configure() {
        bind(ConfigProvider.class).toInstance(provider);
        bindListener(Matchers.any(), new ConfigValueTypeListener(provider));
    }
}
