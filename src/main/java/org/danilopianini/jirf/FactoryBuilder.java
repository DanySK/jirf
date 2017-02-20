package org.danilopianini.jirf;

import java.util.concurrent.Semaphore;
import java.util.function.Function;

import com.google.common.primitives.Primitives;

public class FactoryBuilder {

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
        Primitives.allPrimitiveTypes().forEach(p -> {
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

    //autoboxing
    
    // autounboxing
    
    // withbytesasnumbers
    
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
