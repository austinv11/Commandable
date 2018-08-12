package commandable.aspects;

import commandable.annotations.BindLogic;
import commandable.aspects.impl.GuildOnlyHook;
import commandable.aspects.impl.HelpHook;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindLogic(GuildOnlyHook.class)
public @interface GuildOnly {

}
