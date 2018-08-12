package commandable.annotations;

import java.lang.annotation.Documented;

/**
 * Simple marker annotation denoting that this interface is loaded via a {@link java.util.ServiceLoader} so it should
 * so implementations should be registered appropriately.
 *
 * @see commandable.annotations.WireService
 */
@Documented
public @interface Service {
}
