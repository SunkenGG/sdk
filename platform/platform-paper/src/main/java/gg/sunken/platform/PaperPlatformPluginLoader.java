package gg.sunken.platform;

import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is used to load the plugin on Paper.
 * It extends the SdkPaperPluginLoader class to provide
 *
 * @author santio
 */
@Slf4j
@SuppressWarnings({"UnstableApiUsage", "MissingJavadoc"})
public class PaperPlatformPluginLoader implements PluginLoader {

    private static final String DEPENDENCY_FILE = "/dependencies.json";
    
    @SuppressWarnings("StringConcatenationMissingWhitespace")
    @Override
    public void classloader(@NonNull PluginClasspathBuilder classpathBuilder) {
        final MavenLibraryResolver resolver = new MavenLibraryResolver();
        
        final DependencyInfo dependencyInfo = this.load();
        if (dependencyInfo == null) return;
        
        final AtomicInteger index = new AtomicInteger();
        dependencyInfo.repositories().forEach(repository -> {
            resolver.addRepository(new RemoteRepository.Builder("maven" + index.getAndIncrement(), "default", repository).build());
        });
        
        dependencyInfo.dependencies().forEach(dep -> {
            resolver.addDependency(new Dependency(new DefaultArtifact(dep.toString()), null).setOptional(true));
        });
        
        classpathBuilder.addLibrary(resolver);
    }
    
    @SuppressWarnings("ClassEscapesDefinedScope")
    public @Nullable DependencyInfo load() {
        try (var stream = this.getClass().getResourceAsStream(DEPENDENCY_FILE)) {
            if (stream == null) throw new IllegalStateException("Dependency file not found");
            return new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), DependencyInfo.class);
        } catch (IOException e) {
            log.error("Failed to load dependencies.json", e);
            e.printStackTrace();
            return null;
        }
    }
    
    private record DependencyInfo(
        Set<String> repositories,
        Set<DependencyRef> dependencies
    ) {}

    private record DependencyRef(
        String group,
        String artifact,
        String version
    ) {
        @NonNull
        @Override
        public String toString() {
            return group + ":" + artifact + ":" + version;
        }
    }
}
