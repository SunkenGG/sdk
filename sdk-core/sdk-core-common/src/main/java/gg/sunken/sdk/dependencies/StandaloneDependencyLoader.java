package gg.sunken.sdk.dependencies;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.StandaloneLibraryManager;
import com.alessiodp.libby.logging.adapters.LogAdapter;

import java.nio.file.Path;
import java.util.List;

public class StandaloneDependencyLoader extends DependencyLoader {

    public StandaloneDependencyLoader(LogAdapter logAdapter, Path dataPath) {
        StandaloneLibraryManager libraryManager = new StandaloneLibraryManager(logAdapter, dataPath);

        List<Library> libraries = loadDependencies();
        List<String> list = loadRepositories();

        if (list != null && !list.isEmpty()) {
            for (String repo : list) {
                libraryManager.addRepository(repo);
            }
        }

        if (!libraries.isEmpty()) {
            for (Library library : libraries) {
                try {
                    libraryManager.loadLibrary(library);
                } catch (Exception e) {
                    e.printStackTrace(); // Handle the exception as needed
                }
            }
        }
    }

    @Override
    public List<Library> loadDependencies() {
        List<Library> libraries = loadCommon();

        return libraries;
    }

    @Override
    public List<String> loadRepositories() {
        List<String> repositories = loadCommonRepositories();

        return repositories;
    }
}
