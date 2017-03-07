package org.danilopianini.jirf;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.danilopianini.util.PrimitiveArrays;

import com.google.common.primitives.Primitives;

/**
 * Preconfigures and builds a {@link Factory} implementation.
 */
public class FactoryBuilder {

    private final Factory factory = new FactoryImpl();
    private static final Function<Boolean, Integer> BOOL_TO_INT = b -> b ? 1 : 0;
    private static final Function<Integer, Boolean> INT_TO_BOOL = i -> i == 0;

    private Semaphore mutex = new Semaphore(1);
    private boolean consumed;

    /**
     * Enables the widening conversions that the Java language provides by default.
     * 
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    public FactoryBuilder withWideningConversions() {
        withAutoBoxing();
        factory.registerImplicit(byte.class, short.class, Number::shortValue);
        factory.registerImplicit(byte.class, int.class, Number::intValue);
        factory.registerImplicit(byte.class, long.class, Number::longValue);
        factory.registerImplicit(byte.class, float.class, Number::floatValue);
        factory.registerImplicit(byte.class, double.class, Number::doubleValue);
        factory.registerImplicit(short.class, int.class, Number::intValue);
        factory.registerImplicit(short.class, long.class, Number::longValue);
        factory.registerImplicit(short.class, float.class, Number::floatValue);
        factory.registerImplicit(short.class, double.class, Number::doubleValue);
        factory.registerImplicit(char.class, int.class, Character::getNumericValue);
        factory.registerImplicit(char.class, long.class, c -> (long) Character.getNumericValue(c));
        factory.registerImplicit(char.class, float.class, c -> (float) Character.getNumericValue(c));
        factory.registerImplicit(char.class, double.class, c -> (double) Character.getNumericValue(c));
        factory.registerImplicit(int.class, long.class, Number::longValue);
        factory.registerImplicit(int.class, float.class, Number::floatValue);
        factory.registerImplicit(int.class, double.class, Number::doubleValue);
        factory.registerImplicit(long.class, float.class, Number::floatValue);
        factory.registerImplicit(long.class, double.class, Number::doubleValue);
        factory.registerImplicit(float.class, double.class, Number::doubleValue);
        return this;
    }

    /**
     * Enables the auto (un) boxing that the Java language provides by default.
     * 
     * @param <S>
     *            used internally to make Javac happy
     * 
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    @SuppressWarnings("unchecked")
    public <S> FactoryBuilder withAutoBoxing() {
        Primitives.allPrimitiveTypes().stream()
        .filter(t -> !void.class.equals(t))
        .forEach(p -> {
            factory.<S, S>registerImplicit((Class<S>) p, (Class<S>) Primitives.wrap(p), Function.identity());
            factory.<S, S>registerImplicit((Class<S>) Primitives.wrap(p), (Class<S>) p, Function.identity());
        });
        return this;
    }

    /**
     * Enables the narrowing conversions between primitives. Includes widening
     * conversions.
     * 
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    public FactoryBuilder withNarrowingConversions() {
        withWideningConversions();
        factory.registerImplicit(short.class, byte.class, Number::byteValue);
        factory.registerImplicit(int.class, short.class, Number::shortValue);
        factory.registerImplicit(long.class, int.class, Number::intValue);
        factory.registerImplicit(float.class, long.class, Number::longValue);
        factory.registerImplicit(double.class, long.class, Number::longValue);
        factory.registerImplicit(double.class, float.class, Number::floatValue);
        return this;
    }

    /**
     * Enables autoboxing for linear arrays (e.g. byte[] and Byte[] become interchangeable)
     * 
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    public FactoryBuilder withArrayBoxing() {
        factory.registerImplicit(boolean[].class, Boolean[].class, ArrayUtils::toObject);
        factory.registerImplicit(byte[].class, Byte[].class, ArrayUtils::toObject);
        factory.registerImplicit(short[].class, Short[].class, ArrayUtils::toObject);
        factory.registerImplicit(int[].class, Integer[].class, ArrayUtils::toObject);
        factory.registerImplicit(long[].class, Long[].class, ArrayUtils::toObject);
        factory.registerImplicit(double[].class, Double[].class, ArrayUtils::toObject);
        factory.registerImplicit(float[].class, Float[].class, ArrayUtils::toObject);
        factory.registerImplicit(Boolean[].class, boolean[].class, ArrayUtils::toPrimitive);
        factory.registerImplicit(Byte[].class, byte[].class, ArrayUtils::toPrimitive);
        factory.registerImplicit(Short[].class, short[].class, ArrayUtils::toPrimitive);
        factory.registerImplicit(Integer[].class, int[].class, ArrayUtils::toPrimitive);
        factory.registerImplicit(Long[].class, long[].class, ArrayUtils::toPrimitive);
        factory.registerImplicit(Double[].class, double[].class, ArrayUtils::toPrimitive);
        factory.registerImplicit(Float[].class, float[].class, ArrayUtils::toPrimitive);
        return this;
    }

    /**
     * Enables converting booleans to integers.
     * 
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    public FactoryBuilder withBooleanIntConversions() {
        factory.registerImplicit(boolean.class, int.class, BOOL_TO_INT);
        factory.registerImplicit(int.class, boolean.class, INT_TO_BOOL);
        factory.registerImplicit(Boolean.class, Integer.class, BOOL_TO_INT);
        factory.registerImplicit(Integer.class, Boolean.class, INT_TO_BOOL);
        return this;
    }

    /**
     * Automatically converts objects to {@link String} when needed, by means of {@link Object#toString()}.
     * 
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    public FactoryBuilder withAutomaticToString() {
        factory.registerImplicit(Object.class, String.class, Object::toString);
        return this;
    }

    /**
     * Enables widening conversions for linear arrays (e.g. int[] can get
     * converted to long[]). Includes array autoboxing.
     * 
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    public FactoryBuilder withArrayWideningConversions() {
        withArrayBoxing();
        factory.registerImplicit(byte[].class, short[].class, PrimitiveArrays::toShortArray);
        factory.registerImplicit(byte[].class, int[].class, PrimitiveArrays::toIntArray);
        factory.registerImplicit(byte[].class, long[].class, PrimitiveArrays::toLongArray);
        factory.registerImplicit(byte[].class, float[].class, PrimitiveArrays::toFloatArray);
        factory.registerImplicit(byte[].class, double[].class, PrimitiveArrays::toDoubleArray);
        factory.registerImplicit(short[].class, int[].class, PrimitiveArrays::toIntArray);
        factory.registerImplicit(short[].class, long[].class, PrimitiveArrays::toLongArray);
        factory.registerImplicit(short[].class, float[].class, PrimitiveArrays::toFloatArray);
        factory.registerImplicit(short[].class, double[].class, PrimitiveArrays::toDoubleArray);
        factory.registerImplicit(char[].class, int[].class, PrimitiveArrays::toIntArray);
        factory.registerImplicit(char[].class, long[].class, PrimitiveArrays::toLongArray);
        factory.registerImplicit(char[].class, float[].class, PrimitiveArrays::toFloatArray);
        factory.registerImplicit(char[].class, double[].class, PrimitiveArrays::toDoubleArray);
        factory.registerImplicit(int[].class, long[].class, PrimitiveArrays::toLongArray);
        factory.registerImplicit(int[].class, float[].class, PrimitiveArrays::toFloatArray);
        factory.registerImplicit(int[].class, double[].class, PrimitiveArrays::toDoubleArray);
        factory.registerImplicit(long[].class, float[].class, PrimitiveArrays::toFloatArray);
        factory.registerImplicit(long[].class, double[].class, PrimitiveArrays::toDoubleArray);
        factory.registerImplicit(float[].class, double[].class, PrimitiveArrays::toDoubleArray);
        return this;
    }

    /**
     * Enables narrowing conversions for linear arrays (e.g. double[] can get
     * converted to int[]). Includes widening conversions.
     * 
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    public FactoryBuilder withArrayNarrowingConversions() {
        withArrayWideningConversions();
        factory.registerImplicit(Byte[].class, Number[].class, Function.identity());
        factory.registerImplicit(Short[].class, Number[].class, Function.identity());
        factory.registerImplicit(Integer[].class, Number[].class, Function.identity());
        factory.registerImplicit(Long[].class, Number[].class, Function.identity());
        factory.registerImplicit(Float[].class, Number[].class, Function.identity());
        factory.registerImplicit(Double[].class, Number[].class, Function.identity());
        factory.registerImplicit(Number[].class, byte[].class, PrimitiveArrays::toByteArray);
        factory.registerImplicit(Number[].class, short[].class, PrimitiveArrays::toShortArray);
        factory.registerImplicit(Number[].class, int[].class, PrimitiveArrays::toIntArray);
        factory.registerImplicit(Number[].class, long[].class, PrimitiveArrays::toLongArray);
        factory.registerImplicit(Number[].class, float[].class, PrimitiveArrays::toFloatArray);
        factory.registerImplicit(Number[].class, double[].class, PrimitiveArrays::toDoubleArray);
        return this;
    }


    /**
     * Enables converting linear arrays of booleans to linear arrays of
     * integers. Includes array boxing.
     * 
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    public FactoryBuilder withArrayBooleanIntConversions() {
        withArrayBoxing();
        factory.registerImplicit(boolean[].class, int[].class, PrimitiveArrays::toIntArray);
        factory.registerImplicit(int[].class, boolean[].class, PrimitiveArrays::toBooleanArray);
        factory.registerImplicit(Boolean[].class, Integer[].class, ba -> Arrays.stream(ba).map(BOOL_TO_INT).toArray(Integer[]::new));
        factory.registerImplicit(Integer[].class, Boolean[].class, ba -> Arrays.stream(ba).map(INT_TO_BOOL).toArray(Boolean[]::new));
        return this;
    }

    /**
     * Enables converting linear arrays to lists for the provided classes and
     * Object. Includes array autoboxing.
     * 
     * @param classes
     *            the target classes
     * @return the {@link FactoryBuilder} itself, for method chaining
     */
    public FactoryBuilder withArrayListConversions(final Class<?>... classes) {
        withArrayBoxing();
        for (final Class<?> clazz: classes) {
            if (!clazz.isArray()) {
                throw new IllegalArgumentException("Only array classes can be mapped to Lists");
            }
            factory.registerImplicit(clazz, Object[].class, x -> (Object[]) x);
        }
        factory.registerImplicit(Object[].class, List.class, Arrays::asList);
        factory.registerImplicit(List.class, Object[].class, List::toArray);
        return this;
    }


    /**
     * @return the configured factory. Must be called exactly once.
     */
    public Factory build() {
        checkConsumed();
        return factory;
    }

    private void checkConsumed() {
        mutex.acquireUninterruptibly();
        if (consumed) {
            throw new IllegalStateException("This builder has already been used.");
        }
        consumed = true;
        mutex.release();
    }

}
