package commandable.annotations;

import java.lang.annotation.*;

/**
 * Lightweight alternative to Google's AutoService. This allows for marking a class as a service implementation easily.
 *
 * <b>Note:</b> This requires annotation processing to be active.
 *
 * @see commandable.processor.CommandableProcessor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Repeatable(WireServices.class)
@Documented
public @interface WireService {

    Class<?> value();
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
@interface WireServices {
    WireService[] value();
}
