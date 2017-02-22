package org.danilopianini.jirf;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.primitives.Primitives;

public class FactoryBuilder {

//    private final static String[] ARRAY_ENCODINGS = Arrays.stream(new String[]{
//            "Z", "B", "C", "D", "F", "I", "J", "S"})
//            .map(s -> '[' + s)
//            .toArray(i -> new String[i]);
    private final Factory factory = new FactoryImpl();
    private Semaphore mutex = new Semaphore(1);
    private boolean consumed;

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

    public FactoryBuilder withAllConversions() {
        withWideningConversions();
        factory.registerImplicit(short.class, byte.class, Number::byteValue);
        factory.registerImplicit(int.class, short.class, Number::shortValue);
        factory.registerImplicit(long.class, int.class, Number::intValue);
        factory.registerImplicit(float.class, long.class, Number::longValue);
        factory.registerImplicit(double.class, long.class, Number::longValue);
        factory.registerImplicit(double.class, float.class, Number::floatValue);
        return this;
    }

    public <T> FactoryBuilder withArrayConversions() {
//        withWideningConversions();
//        for (final String encoding: ARRAY_ENCODINGS) {
//            Class<?> arrayClass;
//            try {
//                arrayClass = Class.forName(encoding);
//            } catch (ClassNotFoundException e) {
//                throw new IllegalStateException("There is a bug in the " + getClass().getSimpleName());
//            }
//            factory.registerImplicit(arrayClass, List.class, org.apache.commons.lang3.ArrayUtils::toObject);
//        }

        factory.registerImplicit(boolean[].class, Boolean[].class, ArrayUtils::toObject);
        factory.registerImplicit(byte[].class, Byte[].class, ArrayUtils::toObject);
        factory.registerImplicit(short[].class, Short[].class, ArrayUtils::toObject);
        factory.registerImplicit(int[].class, Integer[].class, ArrayUtils::toObject);
        factory.registerImplicit(long[].class, Long[].class, ArrayUtils::toObject);
        factory.registerImplicit(double[].class, Double[].class, ArrayUtils::toObject);
        factory.registerImplicit(float[].class, Float[].class, ArrayUtils::toObject);
        return this;
    }

    // withboolsasnumbers
    // with lists to arrays
    // withautotostring
    
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
