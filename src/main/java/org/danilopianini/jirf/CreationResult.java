package org.danilopianini.jirf;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Optional;

public interface CreationResult<T> {

    /**
     * @return an {@link Optional} wrapping the object built by the factory, or an empty optional if none was produced
     */
    Optional<T> getCreatedObject();

    /**
     * @return the object built by the factory. If none was built, an exception is thrown.
     */
    T getCreatedObjectOrThrowException();

    /**
     * @return a {@link Map} containing, for each constructor that failed, the failure cause.
     * The iteration order of this map is guaranteed to return its {@link java.util.Map.Entry} in the order they were
     * produced (from the most suitable constructor to the last).
     */
    Map<Constructor<T>, InstancingImpossibleException> getExceptions();
}
