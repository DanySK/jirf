package org.danilopianini.jirf;

import java.lang.reflect.Constructor;

public class InstancingImpossibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InstancingImpossibleException(Constructor<?> constructor, Throwable cause) {
        super("Could not create an instance with " + constructor, cause);
    }

    public InstancingImpossibleException(Constructor<?> constructor, String message) {
        super("Could not create an instance with " + constructor + ": " + message);
    }

}
