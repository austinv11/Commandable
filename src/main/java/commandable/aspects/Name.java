package commandable.aspects;

import commandable.annotations.BindLogic;
import commandable.aspects.impl.NameHook;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindLogic(NameHook.class)
public @interface Name {

    String value();
}
