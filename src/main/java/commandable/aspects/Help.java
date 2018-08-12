package commandable.aspects;

import commandable.annotations.BindLogic;
import commandable.aspects.impl.HelpHook;
import commandable.util.HelpPage;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindLogic(HelpHook.class)
public @interface Help {

    Class<? extends HelpPage> value();
}
