package commandable.processor;

import commandable.annotations.WireService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "commandable.annotations.WireService"
})
public class CommandableProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    public CommandableProcessor() {} // Required

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //Handle @WireService
        Map<String, Set<String>> services = new HashMap<>();
        for (Element annotated : roundEnv.getElementsAnnotatedWith(WireService.class)) {
            if (annotated.getKind() == ElementKind.CLASS) {
    		    WireService[] serviceAnnotations = annotated.getAnnotationsByType(WireService.class);
    		    for (WireService service : serviceAnnotations) {
    		        String serviceName;
    		        try {
    		            serviceName = service.value().getCanonicalName();
                    } catch (MirroredTypeException e) {
    		            serviceName = e.getTypeMirror().toString(); //Yeah, apparently this is the solution you're supposed to use
                    }
    		        if (!services.containsKey(serviceName))
    		            services.put(serviceName, new HashSet<>());
    		        services.get(serviceName).add(annotated.asType().toString());
                }
            }
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "Found " + services.size() + " services!");
        services.forEach((k, v) -> {
            try {
                FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT,
                        "", "META-INF/services" + "/" + k);
                try (Writer w = fo.openWriter()) {
                    for (String impl : v) {
                        messager.printMessage(Diagnostic.Kind.NOTE, "Setting up " + impl + " for use as a " + k + " implementation!");
                        w.append(impl).append("\n");
                    }
                }
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        });
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }
}
