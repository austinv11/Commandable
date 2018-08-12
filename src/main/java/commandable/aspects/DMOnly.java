package commandable.aspects;

import commandable.annotations.BindLogic;
import commandable.aspects.impl.DMOnlyHook;
import commandable.aspects.impl.GuildOnlyHook;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindLogic(DMOnlyHook.class)
public @interface DMOnly {

}
