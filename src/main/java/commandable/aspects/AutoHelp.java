package commandable.aspects;

import commandable.annotations.BindLogic;
import commandable.aspects.impl.AutoHelpHook;
import commandable.aspects.impl.HelpHook;
import commandable.util.HelpPage;

import javax.annotation.Nullable;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindLogic(AutoHelpHook.class)
public @interface AutoHelp {

    String value() default ""; //Description

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface ParameterName {
        String value();
    }
}
