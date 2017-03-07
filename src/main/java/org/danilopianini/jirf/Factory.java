package org.danilopianini.jirf;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.reflect.TypeToken;

public interface Factory {

    <E> void registerSingleton(Class<? super E> lowerBound, Class<? super E> upperBound, E object);

    <E> void registerSingleton(Class<? super E> bound, E object);

    <E> void registerSingleton(E object);

    <E> boolean deregisterSingleton(E object);

    <E> E build(Class<E> clazz, Object... parameters);

    <E> E build(Class<E> clazz, List<?> args);

    <I, O> Optional<O> convert(Class<O> clazz, I target);

    <I, O> O convertOrFail(Class<O> clazz, I target);

    <S, D> void registerImplicit(Class<S> source, Class<D> destination, Function<? super S, ? extends D> implicit);

}
