package commandable.aspects;

import commandable.annotations.BindLogic;
import commandable.aspects.impl.RequiresPermissionsHook;
import discord4j.core.object.util.Permission;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@BindLogic(RequiresPermissionsHook.class)
public @interface RequiresPermissions {

    Permission[] value();
}
