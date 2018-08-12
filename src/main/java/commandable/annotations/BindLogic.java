package commandable.annotations;

import commandable.aspects.AnnotationHook;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BindLogic {

    Class<? extends AnnotationHook> value();
}
