package dk.nodes.nstack.util.translation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by joso on 25/02/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Translate {
    enum AccessLevel { PUBLIC, PROTECTED, PACKAGE_PROTECTED, PRIVATE};

    String value() default "";

    String toggleOn() default "";
    String toggleOff() default "";
}
