package org.danilopianini.jirf;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Optional;

/**
 * The result of the object creation process.
 * In case of success, an {@link Optional} gets populated with the created object,
 * the result is {@link Optional#empty()} instead.
 *
 * Since multiple constructors could have been tried before achieving success or having to fail,
 * a {@link Map} mapping all the constructors to the error experienced on invocation is returned
 * by getExceptions.
 *
 * @param <T> the created object type
 */
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
