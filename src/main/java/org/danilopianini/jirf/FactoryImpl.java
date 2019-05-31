package org.danilopianini.jirf;

import org.apache.commons.lang3.ClassUtils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

final class FactoryImpl implements Factory {

    private final Map<Class<?>, Object> singletons = new LinkedHashMap<>();
    private final Graph<Class<?>, FunctionEdge> implicits = new DefaultDirectedGraph<>(null);
    private static final String UNCHECKED = "unchecked";

    @SuppressWarnings(UNCHECKED)
    @Override
    public <E> E build(final Class<E> clazz, final List<?> args) {
        registerHierarchy(clazz);
        return getFromStaticSources(clazz)
            .orElseGet(() -> {
                final Constructor<E>[] constructors = (Constructor<E>[]) clazz.getConstructors();
                final List<Throwable> exceptions = new LinkedList<>();
                return Arrays.stream(constructors)
                        .map(c -> new ConstructorBenchmark<>(c, args))
                        .filter(cb -> cb.score >= 0)
                        .sorted()
                        .map(cb -> createBestEffort(cb.constructor, args))
                        .filter(e -> {
                            if (e instanceof Throwable) {
                                exceptions.add((Throwable) e);
                                return false;
                            }
                            return true;
                        })
                        .map(e -> (E) e)
                        .findFirst()
                        .orElseThrow(() -> {
                            final IllegalArgumentException ex = new IllegalArgumentException("Cannot create "
                                + clazz.getName()
                                + " with arguments ["
                                + args.stream()
                                    .map(o -> o == null ? "null" : o.toString() + ':' + o.getClass().getSimpleName())
                                    .collect(Collectors.joining(", "))
                                + ']');
                            exceptions.forEach(ex::addSuppressed);
                            return ex;
                        });
            });
    }

    @Override
    public <E> E build(final Class<E> clazz, final Object... parameters) {
        return build(clazz, Arrays.asList(parameters));
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public <I, O> Optional<O> convert(final Class<O> destination, final I source) {
        return findConversionChain(Objects.requireNonNull(source.getClass()), Objects.requireNonNull(destination))
            .map(chain -> {
                Object in = source;
                for (final FunctionEdge implicit : chain.getEdgeList()) {
                    in = ((Function<Object, ?>) implicit.getFunction()).apply(in);
                }
                return (O) in;
            });
    }

    @SuppressWarnings(UNCHECKED)
    private <E> Optional<E> getSingleton(final Class<? super E> clazz) {
        return Optional.ofNullable((E) singletons.get(clazz));
    }

    private <E> Optional<E> getFromStaticSources(final Class<E> clazz) {
        return getSingleton(clazz);
    }

    private Object createBestEffort(final Constructor<?> constructor, final List<?> params) {
        final Deque<?> paramsLeft = new LinkedList<>(params);
        final Class<?>[] expectedTypes = constructor.getParameterTypes();
        final Object[] actualArgs = new Object[expectedTypes.length];
        final boolean varArgs = constructor.isVarArgs();
        for (int i = 0; i < expectedTypes.length; i++) {
            final Class<?> expected = expectedTypes[i];
            final Optional<?> single = getFromStaticSources(expected);
            if (single.isPresent()) {
                actualArgs[i] = single.get();
            } else {
                if (varArgs && i == expectedTypes.length - 1) {
                    /*
                     * Last parameter, and it is a varargs. The strategy is to
                     * first try to use the parameter as-is if there is only one
                     * parameter left, going through the conversions. Otherwise,
                     * trying to build an array with the remaining parameters.
                     */
                    final Class<?> type = expected.getComponentType();
                    Object varargs = null;
                    if (paramsLeft.size() == 1) {
                        final Object lastParam = paramsLeft.peek();
                        final Object converted = convertIfNeeded(lastParam, expected, constructor);
                        if (!(converted instanceof InstancingImpossibleException)) {
                            paramsLeft.pop();
                            varargs = converted;
                        }
                    }
                    final int left = paramsLeft.size();
                    varargs = varargs == null ? Array.newInstance(type, left) : varargs;
                    for (int pn = 0; pn < left; pn++) {
                        final Object param = convertIfNeeded(paramsLeft.pop(), type, constructor);
                        if (param instanceof InstancingImpossibleException) {
                            return param;
                        }
                        Array.set(varargs, pn, param);
                    }
                    actualArgs[i] = varargs;
                } else {
                    final Object param = convertIfNeeded(paramsLeft.pop(), expected, constructor);
                    if (param instanceof InstancingImpossibleException) {
                        return param;
                    }
                    actualArgs[i] = param;
                }
            }
        }
        try {
            return constructor.newInstance(actualArgs);
        } catch (Exception e) { // NOPMD
            return new InstancingImpossibleException(constructor, e);
        }
    }

    private Object convertIfNeeded(final Object param, final Class<?> expected, final Constructor<?> constructor) {
        if (param == null || expected.isAssignableFrom(param.getClass())) {
            return param;
        } else {
            final Optional<?> result = convert(expected, param);
            if (result.isPresent()) {
                return result.get();
            }
            return new InstancingImpossibleException(constructor,
                    "Couldn't convert " + param
                    + " from " + param.getClass().getName()
                    + " to " + expected.getName());
        }
    }

    private <S, D> Optional<GraphPath<Class<?>, FunctionEdge>> findConversionChain(final Class<S> source, final Class<D> destination) {
        registerHierarchy(source);
        registerHierarchy(destination);
        return Optional.ofNullable(DijkstraShortestPath.findPathBetween(implicits, source, destination));
    }

    @Override
    public <S, D> void registerImplicit(
            final Class<S> source,
            final Class<D> target,
            final Function<? super S, ? extends D> implicit) {
        registerHierarchy(source);
        registerHierarchy(target);
        addEdge(source, target, implicit);
    }

    private <S, D> void addEdge(
            final Class<S> source,
            final Class<D> target,
            final Function<? super S, ? extends D> implicit) {
        implicits.removeEdge(source, target);
        Objects.requireNonNull(implicits.addEdge(source, target, new FunctionEdge(source, target, implicit)));
    }

    @SuppressWarnings(UNCHECKED)
    private <T> void registerHierarchy(final Class<T> x) {
        assert x != null;
        if (!implicits.containsVertex(x)) {
            implicits.addVertex(x);
            final Class<? super T> superclass = x.getSuperclass();
            if (superclass != null) {
                registerHierarchy(superclass);
                addEdge(x, superclass, Function.identity());
            } else if (x.isInterface()) {
                addEdge(x, Object.class, Function.identity());
            }
            for (@SuppressWarnings("rawtypes") final Class iface: x.getInterfaces()) {
                registerHierarchy(iface);
                addEdge(x, iface, Function.identity());
            }
        }
    }

    @Override
    public <E> void registerSingleton(
            final Class<? super E> lowerBound,
            final Class<? super E> upperBound,
            final E object) {
        register(singletons, Objects.requireNonNull(lowerBound),
                Objects.requireNonNull(upperBound),
                (Class<E>) Objects.requireNonNull(object).getClass(), object);
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public <E> void registerSingleton(final Class<? super E> bound, final E object) {
        registerSingleton((Class<E>) Objects.requireNonNull(object).getClass(), bound, object);
    }

    @SuppressWarnings(UNCHECKED)
    @Override
    public <E> void registerSingleton(final E object) {
        registerSingleton((Class<E>) Objects.requireNonNull(object).getClass(), object);
    }

    private static void checkSuperclass(final Class<?> lower, final Class<?> upper) {
        if (!upper.isAssignableFrom(lower)) {
            throw new IllegalArgumentException(upper + " must be a superclass of " + lower);
        }
    }

    private static <E, O> void register(
            final Map<Class<?>, O> map,
            final Class<? super E> lowerbound,
            final Class<? super E> upperbound,
            final Class<? super E> clazz,
            final O object) {
        checkSuperclass(Objects.requireNonNull(clazz), Objects.requireNonNull(lowerbound));
        checkSuperclass(lowerbound, Objects.requireNonNull(upperbound));
        for (Class<? super E> c = lowerbound; c != null && upperbound.isAssignableFrom(c); c = c.getSuperclass()) {
            map.put(c, Objects.requireNonNull(object));
        }
        if (upperbound.isInterface()) {
            final boolean li = lowerbound.isInterface();
            for (final Class<?> i : ClassUtils.getAllInterfaces(clazz)) {
                if (upperbound.isAssignableFrom(i) && (!li || i.isAssignableFrom(lowerbound))) {
                    map.put(i, object);
                }
            }
        }
    }

    private final class ConstructorBenchmark<T> implements Comparable<ConstructorBenchmark<T>> {
        private final Constructor<T> constructor;
        private final int score;

        ConstructorBenchmark(final Constructor<T> constructor, final List<?> args) {
            this.constructor = constructor;
            /*
             * Filter out constructor arguments that will be assigned to singletons
             */
            final Class<?>[] filteredParams = Arrays.stream(constructor.getParameterTypes())
                    .filter(clazz -> !singletons.containsKey(clazz))
                    .toArray(Class[]::new);
            score = computeScore(filteredParams, args);
        }

        @Override
        public int compareTo(final ConstructorBenchmark<T> o) {
            return score == o.score
                    ? constructor.toString().compareTo(o.constructor.toString())
                    : Integer.compare(score, o.score);
        }

        private int computeScore(final Class<?>[] filteredParams, final List<?> args) {
            final int argsSize = args.size();
            final int numDiff = argsSize - filteredParams.length; // Passed - expected
            if (numDiff == 0 || numDiff == -1 && constructor.isVarArgs()) { // Consider empty varargs
                int tempScore = 0;
                for (int i = 0; i < argsSize; i++) {
                    final Object param = args.get(i);
                    final Class<?> expected = filteredParams[i];
                    if (param == null) {
                        if (expected.isPrimitive()) {
                            return -1;
                        }
                    } else {
                        final Class<?> actual = param.getClass();
                        if (!expected.isAssignableFrom(actual)) {
                            tempScore += findConversionChain(actual, expected)
                                .map(GraphPath::getLength)
                                .orElse(implicits.edgeSet().size());
                        }
                    }
                }
                return tempScore;
            } else {
                return numDiff > 0 ? Short.MAX_VALUE + numDiff : -1;
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
        public String toString() {
            return constructor + "->" + score;
        }
    }

    @Override
    public <I, O> O convertOrFail(final Class<O> clazz, final I target) {
        return this.convert(clazz, target)
                .orElseThrow(() -> new IllegalArgumentException("Unable to convert " + target + " to " + clazz));
    }

    @Override
    public <E> boolean deregisterSingleton(final E object) {
        final Iterator<Object> regSingletons = singletons.values().iterator();
        boolean found = false;
        while (regSingletons.hasNext()) {
            final Object singleton = regSingletons.next();
            if (singleton.equals(object)) {
                regSingletons.remove();
                found = true;
            }
        }
        return found;
    }

}
