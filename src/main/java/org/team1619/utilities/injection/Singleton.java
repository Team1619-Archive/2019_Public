package org.team1619.utilities.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Tells the injector to only make one instance of this class
 */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RUNTIME)
public @interface Singleton {
}