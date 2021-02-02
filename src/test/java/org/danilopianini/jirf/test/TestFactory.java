package org.danilopianini.jirf.test;

import com.google.common.collect.ImmutableList;
import org.danilopianini.jirf.Factory;
import org.danilopianini.jirf.FactoryBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertNotNull;

/**
 *
 */
public final class TestFactory {

    /**
     * 
     */
    @Test
    public void testWideningPrimitivesAndWrappers() {
        final Factory f = new FactoryBuilder()
                .withWideningConversions()
                .build();
        assertNotNull(f.build(MyObj.class, 1, 2, (byte) 3).getCreatedObjectOrThrowException());
    }

    /**
     * 
     */
    @Test
    public void testNonWidening() {
        final Factory f = new FactoryBuilder()
                .withNarrowingConversions()
                .build();
        assertNotNull(f.build(MyObj.class, 1, 2, 3.0).getCreatedObjectOrThrowException());
    }

    /**
     * 
     */
    @Test
    public void testEmptyVarargs() {
        final Factory f = new FactoryBuilder()
                .withAutoBoxing()
                .withWideningConversions()
                .build();
        assertNotNull(f.build(MyObj.class, "").getCreatedObjectOrThrowException());
    }

    /**
     * 
     */
    @Test
    public void testSingleValuedVarargs() {
        final Factory f = new FactoryBuilder()
                .withAutoBoxing()
                .withWideningConversions()
                .build();
        assertNotNull(f.build(MyObj.class, "", 1).getCreatedObjectOrThrowException());
    }

    /**
     * 
     */
    @Test
    public void testExpandedVarargs() {
        final Factory f = new FactoryBuilder()
                .withAutoBoxing()
                .withWideningConversions()
                .build();
        assertNotNull(f.build(MyObj.class, "", 1, 2, 3, 4).getCreatedObjectOrThrowException());
    }

    /**
     * 
     */
    @Test
    public void testListEmbeddedVarargs() {
        final Factory f = new FactoryBuilder()
                .withAutoBoxing()
                .withWideningConversions()
                .withArrayListConversions(double[].class)
                .build();
        assertNotNull(
            f.build(MyObj.class, "", ImmutableList.of(1, 2, 3, 4)).getCreatedObjectOrThrowException()
        );
    }

    /**
     * 
     */
    @Test
    public void testSuperclasses() {
        final Factory f = new FactoryBuilder()
                .withAutoBoxing()
                .build();
        f.registerImplicit(CharSequence.class, double.class, s -> Double.parseDouble(s.toString()));
        assertNotNull(f.build(MyObj.class, "1", "2", (byte) 3).getCreatedObjectOrThrowException());
    }

    /**
     * 
     */
    @Test
    public void testSingleton() {
        final Factory f = new FactoryBuilder()
                .build();
        final Object o = new Object();
        f.registerSingleton(o);
        Assert.assertSame(o, f.build(Object.class).getCreatedObjectOrThrowException());
        final BigInteger s = new BigInteger("25");
        f.registerSingleton(Number.class, Object.class, s);
        Assert.assertSame(s, f.build(Number.class).getCreatedObjectOrThrowException());
        Assert.assertSame(s, f.build(Object.class).getCreatedObjectOrThrowException());
        final var bigInteger = f.build(BigInteger.class);
        Assert.assertTrue(bigInteger.getCreatedObject().isEmpty());
        assertNotNull(bigInteger.getExceptions());
        Assert.assertFalse(bigInteger.getExceptions().isEmpty());
        final var bigInteger2 = f.build(BigInteger.class, "ciao");
        Assert.assertTrue(bigInteger2.getCreatedObject().isEmpty());
        Assert.assertFalse(bigInteger2.getExceptions().isEmpty());
    }

    // CHECKSTYLE: EmptyStatement OFF
    // CHECKSTYLE: NeedBraces OFF
    // CHECKSTYLE: JavadocType OFF
    // CHECKSTYLE: JavadocMethod OFF
    public static final class MyObj {
        public MyObj(final double a, final Double b, final byte c) { } // NOPMD
        public MyObj(final String a, final double... b) { // NOPMD
            for (int i = 0; i < b.length; i++); // NOPMD
        }
    }

}
