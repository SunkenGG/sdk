package gg.sunken.sdk.guice.processor;


import gg.sunken.sdk.guice.annotations.GuiceFactory;
import com.google.auto.service.AutoService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Annotation processor for discovering guice factories and registering them as a service
 *
 * @author santio
 */
@AutoService(Processor.class)
@Accessors(fluent = true)
@SupportedAnnotationTypes("gg.sunken.sdk.guice.annotations.GuiceFactory")
public class GuiceFactoryAnnotationProcessor extends AbstractProcessor {

    @Getter
    private static final String GUICE_FACTORY_CLASSES = "META-INF/guice/factories";
    private final Set<String> factoryClasses = new HashSet<>();
    
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(GuiceFactory.class.getName());
    }
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @SneakyThrows
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            this.createFile();
        } else {
            this.processAnnotations(roundEnv);
        }

        return false;
    }

    @SuppressWarnings("NestedTryStatement")
    private void createFile() {
        final Filer filer = this.processingEnv.getFiler();

        try {
            final FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", GUICE_FACTORY_CLASSES);
            try (final BufferedWriter writer = new BufferedWriter(file.openWriter())) {
                for (String factoryClass : this.factoryClasses) {
                    writer.write(factoryClass + "\n");
                }
            }
        } catch (IOException e) {
            this.processingEnv.getMessager().printError("Failed to write factory discovery file: " + e.getMessage());
        }
    }

    private void processAnnotations(RoundEnvironment roundEnv) {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GuiceFactory.class);

        for (final Element element : elements) {
            if (element.getKind() != ElementKind.INTERFACE) {
                this.processingEnv.getMessager().printError("Only interfaces can be annotated with @Entity", element);
                continue;
            }

            final TypeElement typeElement = (TypeElement) element;

            final String nestedNotation = typeElement.getQualifiedName().toString().replace(
                "." + typeElement.getSimpleName(),
                "$" + typeElement.getSimpleName()
            );

            this.factoryClasses.add(
                typeElement.getNestingKind().isNested()
                    ? nestedNotation
                    : typeElement.getQualifiedName().toString()
            );
        }
    }
}