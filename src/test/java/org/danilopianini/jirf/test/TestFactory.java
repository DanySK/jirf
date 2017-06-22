package org.danilopianini.jirf.test;

import java.math.BigInteger;

import org.danilopianini.jirf.Factory;
import org.danilopianini.jirf.FactoryBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

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
        Assert.assertNotNull(f.build(MyObj.class, 1, 2, (byte) 3));
    }

    /**
     * 
     */
    @Test
    public void testNonWidening() {
        final Factory f = new FactoryBuilder()
                .withNarrowingConversions()
                .build();
        Assert.assertNotNull(f.build(MyObj.class, 1, 2, 3.0));
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
        Assert.assertNotNull(f.build(MyObj.class, ""));
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
        Assert.assertNotNull(f.build(MyObj.class, "", 1));
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
        Assert.assertNotNull(f.build(MyObj.class, "", 1, 2, 3, 4));
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
        Assert.assertNotNull(f.build(MyObj.class, "", ImmutableList.of(1, 2, 3, 4)));
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
        Assert.assertNotNull(f.build(MyObj.class, "1", "2", (byte) 3));
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
        Assert.assertSame(o, f.build(Object.class));
        final BigInteger s = new BigInteger("25");
        f.registerSingleton(Number.class, Object.class, s);
        Assert.assertSame(s, f.build(Number.class));
        Assert.assertSame(s, f.build(Object.class));
        try {
            Assert.assertNotEquals(s, f.build(BigInteger.class));
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }
        try {
            Assert.assertNotEquals(s, f.build(BigInteger.class, "ciao"));
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    // CHECKSTYLE:OFF
    public static class MyObj {
        public MyObj(final double a, final Double b, final byte c) { } // NOPMD
        public MyObj(final String a, final double... b) {
            for (int i = 0; i < b.length; i++); // NOPMD
        }
    }

}
