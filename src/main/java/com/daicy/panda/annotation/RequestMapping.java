package com.daicy.panda.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author: create by daichangya
 * @version: v1.0
 * @description: com.daicy.panda.annotation
 * @date:19-11-6
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface RequestMapping {
    /**
     * Assign a name to this mapping.
     * <p><b>Supported at the type level as well as at the method level!</b>
     * When used on both levels, a combined name is derived by concatenation
     * with "#" as separator.
     */
    String name() default "";

    /**
     * The primary mapping expressed by this annotation.
     * {@code @RequestMapping("/foo")} is equivalent to
     * {@code @RequestMapping(path="/foo")}.
     * <p><b>Supported at the type level as well as at the method level!</b>
     * When used at the type level, all method-level mappings inherit
     * this primary mapping, narrowing it for a specific handler method.
     */
    String[] value() default {};


    /**
     * The HTTP request methods to map to, narrowing the primary mapping:
     * GET, POST, HEAD, OPTIONS, PUT, PATCH, DELETE, TRACE.
     * <p><b>Supported at the type level as well as at the method level!</b>
     * When used at the type level, all method-level mappings inherit
     * this HTTP method restriction (i.e. the type-level restriction
     * gets checked before the handler method is even resolved).
     */
    RequestMethod[] method() default {};
}
