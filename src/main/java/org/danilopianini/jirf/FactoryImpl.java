package org.danilopianini.jirf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FactoryImpl implements Factory {

    private static final Logger L = LoggerFactory.getLogger(Factory.class);

    private final Map<Class<?>, Object> singletons = new LinkedHashMap<>();
    private final Map<Class<?>, Supplier<?>> suppliers = new LinkedHashMap<>();
    private final ImplicitEdgeFactory edgeFactory = new ImplicitEdgeFactory();
    private final Graph<Class<?>, Function<?, ?>> implicits = new DefaultDirectedGraph<>(edgeFactory);

    public FactoryImpl() {
        // TODO Auto-generated constructor stub
    }

    private static void checkSuperclass(final Class<?> lower, final Class<?> upper) {
        if (upper.isAssignableFrom(lower)) {
            throw new IllegalArgumentException(upper + " must be a superclass of " + lower);
        }
    }
    
    private static <E, O> void register(
            final Map<Class<?>, O> map,
            final Class<? super E> lowerbound,
            final Class<? super E> upperbound,
            final Class<? super E> clazz,
            final O object) {
        checkSuperclass(Objects.requireNonNull(lowerbound), Objects.requireNonNull(clazz));
        checkSuperclass(Objects.requireNonNull(upperbound), lowerbound);
        for (Class<? super E> c = lowerbound; c != null && upperbound.isAssignableFrom(c); c = c.getSuperclass()) {
            map.put(c, Objects.requireNonNull(object));
        }
    }

    private <S, D> Optional<List<Function<?, ?>>> findConversionChain(final Class<S> source, final Class<D> destination) {
        return Optional.ofNullable(DijkstraShortestPath.findPathBetween(implicits, source.getClass(), destination));
    }

    @SuppressWarnings("unchecked")
    private <S, D> Optional<D> convert(final S source, final Class<D> destination) {
        return findConversionChain(source.getClass(), destination)
            .map(chain -> {
                Object in = source;
                for (final Function<?, ?> implicit : chain) {
                    in = ((Function<Object, ?>) implicit).apply(in);
                }
                return (D) in;
            });
    }

    @Override
    public <E> E build(final Class<E> clazz, final Object... parameters) {
        return build(clazz, Arrays.asList(parameters));
    }

    @Override
    public <E> E build(final Class<? super E> clazz, final List<?> args) {
        @SuppressWarnings("unchecked")
        final Constructor<E>[] constructors = (Constructor<E>[]) clazz.getConstructors();
        return Arrays.stream(constructors)
                .map(c -> new ConstructorBenchmark<E>(c, args))
                .sorted()
                .map(cb -> createBestEffort(cb.constructor, args))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }

    private <O> Optional<O> createBestEffort(final Constructor<O> constructor, final List<?> params) {
        final Deque<?> paramsLeft = new LinkedList<>(Collections.unmodifiableList(params));
        final Object[] actualArgs = Arrays.stream(constructor.getParameterTypes())
            .map(expectedClass -> {
                /*
                 * Singletons have priority
                 */
                final Object singleton = singletons.get(expectedClass);
                if (singleton != null) {
                    return singleton;
                }
                /*
                 * Then, try to use the next available parameter
                 */
                final Object param = paramsLeft.pop();
                if (param != null) {
                    if (expectedClass.isAssignableFrom(param.getClass())) {
                        return param;
                    }
                    /*
                     * Try an implicit conversion
                     */
                    final Optional<?> result = convert(param, expectedClass);
                    if (result.isPresent()) {
                        return result.get();
                    }
                }
                /*
                 * Check if the object can be supplied
                 */
                final Supplier<?> supplier = suppliers.get(expectedClass);
                if (supplier != null) {
                    return supplier.get();
                }
                /*
                 * Give up.
                 */
                return null;
            }).toArray();
        try {
            final O result = constructor.newInstance(actualArgs);
            L.debug("{} produced {} with arguments {}", constructor, result, actualArgs);
            return Optional.of(result);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            L.debug("No luck with {} and arguments {}", constructor, actualArgs);
        }
        return Optional.empty();
    }

    private class ConstructorBenchmark<T> implements Comparable<ConstructorBenchmark<T>> {
        private final Constructor<T> constructor;
        private final int score;

        ConstructorBenchmark(final Constructor<T> constructor, final Object... args) {
            this.constructor = constructor;
            /*
             * Filter out constructor arguments that will be assigned to singletons
             */
            final Class<?>[] filteredParams = Arrays.stream(constructor.getParameterTypes())
                    .filter(clazz -> !singletons.containsKey(clazz))
                    .toArray(i -> new Class<?>[i]);
            score = computeScore(filteredParams, args);
        }

        private int computeScore(final Class<?>[] filteredParams, final Object... args) {
            final int numDiff = filteredParams.length - args.length;
            if (numDiff == 0) {
                int tempScore = 0;
                for (int i = 0; i < args.length; i++) {
                    final Class<?> expected = filteredParams[i];
                    final Class<?> actual = args[i].getClass();
                    if (!expected.isAssignableFrom(actual)) {
                        tempScore += findConversionChain(actual, expected)
                            .map(List::size)
                            .orElse(100);
                    }
                }
                return tempScore;
            } else {
                return Short.MAX_VALUE + numDiff;
            }
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof ConstructorBenchmark
                    && constructor.equals(((ConstructorBenchmark<?>) obj).constructor)
                    && score == ((ConstructorBenchmark<?>) obj).score;
        }

        @Override
        public int hashCode() {
            return constructor.hashCode() ^ score;
        }

        @Override
        public int compareTo(final ConstructorBenchmark<T> o) {
            return score == o.score
                    ? constructor.toString().compareTo(o.constructor.toString())
                    : Integer.compare(score, o.score);
        }
    }

    @Override
    public <E> void registerSingleton(
            final Class<? super E> lowerBound,
            final Class<? super E> upperBound,
            final E object) {
        register(singletons, lowerBound, upperBound, object.getClass(), object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> void registerSingleton(final Class<? super E> bound, final E object) {
        registerSingleton((Class<E>) object.getClass(), bound, object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> void registerSingleton(final E object) {
        registerSingleton((Class<E>) object.getClass(), object);
    }

    @Override
    public <E> void registerSupplier(
            final Class<? super E> lowerBound,
            final Class<? super E> upperBound,
            final Class<? super E> clazz,
            final Supplier<? extends E> supplier) {
        register(suppliers, lowerBound, upperBound, clazz, supplier);
    }

    @Override
    public <E> void registerSupplier(
            final Class<? super E> bound,
            final Class<? super E> clazz,
            final Supplier<? extends E> object) {
        registerSupplier(clazz, bound, clazz, object);
    }

    @Override
    public <E> void registerSupplier(
            final Class<? super E> clazz,
            final Supplier<? extends E> object) {
        registerSupplier(clazz, clazz, object);
    }

}
