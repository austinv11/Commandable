package commandable.aspects;

import commandable.annotations.BindLogic;
import commandable.aspects.impl.OwnerOnlyHook;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindLogic(OwnerOnlyHook.class)
public @interface OwnerOnly {

}
