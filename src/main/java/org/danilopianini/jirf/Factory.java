package org.danilopianini.jirf;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Instance builder for arbitrary Java classes with partly specified parameters.
 */
public interface Factory {

    /**
     * @param clazz
     *            the {@link Class} of which an instance should get built
     * @param args
     *            the arguments. They should be in order. Arguments that can be
     *            deduced automatically as singletons must not be included. The
     *            implicit conversions are automatically applied, in case
     *            multiple of them are available, the shortest path on the
     *            conversion graph is used.
     * @param <E>
     *            the Object type
     * @return the instance
     * @throws IllegalArgumentException
     *             if for any reason the class can't get instanced with the
     *             provided parameters and the configured singletons and
     *             implicit conversions
     */
    <E> E build(Class<E> clazz, List<?> args);

    /**
     * @param clazz
     *            the {@link Class} of which an instance should get built
     * @param parameters
     *            the arguments. They should be in order. Arguments that can be
     *            deduced automatically as singletons must not be included. The
     *            implicit conversions are automatically applied, in case
     *            multiple of them are available, the shortest path on the
     *            conversion graph is used.
     * @param <E>
     *            the Object type
     * @return the instance
     * @throws IllegalArgumentException
     *             if for any reason the class can't get instanced with the
     *             provided parameters and the configured singletons and
     *             implicit conversions
     */
    <E> E build(Class<E> clazz, Object... parameters);

    /**
     * Applies the available implicit conversions and tries to convert the
     * provided object to the provided class.
     * 
     * @param clazz
     *            the target {@link Class}
     * @param target
     *            the object that should get converted.
     * @param <I>
     *            the input Object type
     * @param <O>
     *            the output Object type
     * @return the converted object if it was possible to convert, or
     *         {@link Optional#empty()} if the conversion was not possible
     */
    <I, O> Optional<O> convert(Class<O> clazz, I target);

    /**
     * Applies the available implicit conversions and tries to convert the
     * provided object to the provided class.
     * 
     * @param clazz
     *            the target {@link Class}
     * @param target
     *            the object that should get converted.
     * @param <I>
     *            the input Object type
     * @param <O>
     *            the output Object type
     * @return the converted object if it was possible to convert, or
     *         {@link Optional#empty()} if the conversion was not possible
     * @throws IllegalArgumentException
     *             if for any reason the class can't get converted with the
     *             configured implicit conversions
     */
    <I, O> O convertOrFail(Class<O> clazz, I target);

    /**
     * Removes a registered singleton.
     * 
     * @param object
     *            the object that should be forgot
     * @param <E>
     *            the Object type
     * @return true if the {@link Object} was successfully de-registered
     */
    <E> boolean deregisterSingleton(E object);

    /**
     * Registers an implicit conversion function. All the superclasses and
     * implemented interfaces of the source are registered as well as
     * convertible.
     * 
     * @param source
     *            origin class
     * @param destination
     *            destination class
     * @param implicit
     *            the conversion {@link Function}
     * @param <S>
     *            Input type
     * @param <D>
     *            Output type
     */
    <S, D> void registerImplicit(Class<S> source, Class<D> destination, Function<? super S, ? extends D> implicit);

    /**
     * Registers a singleton. It gets mapped to every class from the provided
     * lower bound and the provided upper bound (both must be superclasses of
     * the object's own class). If the bound is an interface, all the
     * sub-interfaces that the class implements get registered too.
     * 
     * @param lowerBound
     *            the lower bound class
     * @param upperBound
     *            the upper bound class
     * @param object
     *            the object that should be registered
     * @param <E>
     *            the Object type
     */
    <E> void registerSingleton(Class<? super E> lowerBound, Class<? super E> upperBound, E object);

    /**
     * Registers a singleton. It gets mapped to every class from its own
     * (discovered by using {@link Object#getClass()} and the provided upper
     * bound. If the bound is an interface, all the sub-interfaces that the
     * class implements get registered too.
     * 
     * @param bound
     *            the upper bound class
     * @param object
     *            the object that should be registered
     * @param <E>
     *            the Object type
     */
    <E> void registerSingleton(Class<? super E> bound, E object);

    /**
     * Registers a singleton. It only gets mapped to its own class (discovered by using {@link Object#getClass()}.
     * 
     * @param object
     *            the object that should be registered
     * @param <E>
     *            the Object type
     */
    <E> void registerSingleton(E object);

}
