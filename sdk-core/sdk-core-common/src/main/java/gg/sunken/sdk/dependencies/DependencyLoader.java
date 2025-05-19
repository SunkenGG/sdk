package gg.sunken.sdk.dependencies;

import com.alessiodp.libby.Library;

import java.util.ArrayList;
import java.util.List;

public abstract class DependencyLoader {

    public DependencyLoader() {}

    public abstract List<Library> loadDependencies();

    public abstract List<String> loadRepositories();

    protected List<String> loadCommonRepositories() {
        List<String> repositories = new ArrayList<>();
        repositories.add("https://repo1.maven.org/maven2/");
        repositories.add("https://redempt.dev");
        repositories.add("https://repo.alessiodp.com/snapshots");
        return repositories;
    }

    protected List<Library> loadCommon() {
        List<Library> libraries = new ArrayList<>();

        libraries.add(loadLibrary(
                DependencyInfo.ANNOTATIONS_GROUP,
                DependencyInfo.ANNOTATIONS_ARTIFACT,
                DependencyInfo.ANNOTATIONS_VERSION,
                "org{}jetbrains{}annotations",
                "gg{}sunken{}sdk{}dependencies{}libs{}annotations"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.GSON_GROUP,
                DependencyInfo.GSON_ARTIFACT,
                DependencyInfo.GSON_VERSION,
                "com{}google",
                "gg{}sunken{}sdk{}dependencies{}libs{}google"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.GUICE_GROUP,
                DependencyInfo.GUICE_ARTIFACT,
                DependencyInfo.GUICE_VERSION,
                "com{}google",
                "gg{}sunken{}sdk{}dependencies{}libs{}google"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.GUICE_ASSISTEDINJECT_GROUP,
                DependencyInfo.GUICE_ASSISTEDINJECT_ARTIFACT,
                DependencyInfo.GUICE_ASSISTEDINJECT_VERSION,
                "com{}google",
                "gg{}sunken{}sdk{}dependencies{}libs{}google"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.MONGO_GROUP,
                DependencyInfo.MONGO_ARTIFACT,
                DependencyInfo.MONGO_VERSION,
                "org{}mongodb{}mongo-java-driver",
                "gg{}sunken{}sdk{}dependencies{}libs{}mongo"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.INFLUX_GROUP,
                DependencyInfo.INFLUX_ARTIFACT,
                DependencyInfo.INFLUX_VERSION,
                "com{}influxdb",
                "gg{}sunken{}sdk{}dependencies{}libs{}influxdb"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.CRUNCH_GROUP,
                DependencyInfo.CRUNCH_ARTIFACT,
                DependencyInfo.CRUNCH_VERSION,
                "redempt{}crunch",
                "gg{}sunken{}sdk{}dependencies{}libs{}crunch"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.CAFFEINE_GROUP,
                DependencyInfo.CAFFEINE_ARTIFACT,
                DependencyInfo.CAFFEINE_VERSION,
                "com{}github{}benmanes{}caffeine",
                "gg{}sunken{}sdk{}dependencies{}libs{}caffeine"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.BOOSTEDYML_GROUP,
                DependencyInfo.BOOSTEDYML_ARTIFACT,
                DependencyInfo.BOOSTEDYML_VERSION,
                "dev{}dejvokep{}boostedyaml",
                "gg{}sunken{}sdk{}dependencies{}libs{}boostedyaml"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.OBJENESIS_GROUP,
                DependencyInfo.OBJENESIS_ARTIFACT,
                DependencyInfo.OBJENESIS_VERSION,
                "org{}objenesis",
                "gg{}sunken{}sdk{}dependencies{}libs{}objenesis"
        ));

        libraries.add(loadLibrary(
                DependencyInfo.SLF4J_API_GROUP,
                DependencyInfo.SLF4J_API_ARTIFACT,
                DependencyInfo.SLF4J_API_VERSION,
                "org{}slf4j{}slf4j-api",
                "gg{}sunken{}sdk{}dependencies{}libs{}slf4j"
        ));

        return libraries;
    }

    public Library loadLibrary(String groupId, String artifactId, String version, String... relocations) {
        Library.Builder builder = Library.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version);

        for (int i = 0; i < relocations.length; i += 2) {
            if (i + 1 >= relocations.length) {
                throw new IllegalArgumentException("Relocations must be in pairs (source and target). Found odd number of relocations: " + relocations.length + " for " + groupId + ":" + artifactId);
            }
            String source = relocations[i];
            String target = relocations[i + 1];
            builder.relocate(source, target);
        }

        return builder.build();
    }
}
