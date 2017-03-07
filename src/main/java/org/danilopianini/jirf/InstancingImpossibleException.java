package org.danilopianini.jirf;

import java.lang.reflect.Constructor;

/**
 *
 */
public class InstancingImpossibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    InstancingImpossibleException(final Constructor<?> constructor, final Throwable cause) {
        super("Could not create an instance with " + constructor, cause);
    }

    InstancingImpossibleException(final Constructor<?> constructor, final String message) {
        super("Could not create an instance with " + constructor + ": " + message);
    }

}
