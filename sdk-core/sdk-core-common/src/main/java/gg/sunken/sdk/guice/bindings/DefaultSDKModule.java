package gg.sunken.sdk.guice.bindings;

import com.google.auto.service.AutoService;
import com.google.inject.Module;
import gg.sunken.sdk.featureflag.MongoFeatureStore;
import gg.sunken.sdk.io.Request;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * A generic module that binds the SDK to the database. This module should always be added
 * to the injector, as it is required for certain features to work.
 *
 * @author santio
 */
@AutoService(Module.class)
public class DefaultSDKModule extends AbstractModule {

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
    
    @Override
    protected void configure() {
        this.install(new FactoryModuleBuilder().build(MongoFeatureStore.Factory.class));
        this.install(new FactoryModuleBuilder().build(Request.Factory.class));
        
        this.bind(HttpClient.class).toInstance(httpClient);
    }

}