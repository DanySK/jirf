package org.danilopianini.jirf.test;

import java.math.BigInteger;

import org.danilopianini.jirf.Factory;
import org.danilopianini.jirf.FactoryBuilder;
import org.junit.Assert;
import org.junit.Test;

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
                .withAllConversions()
                .build();
        Assert.assertNotNull(f.build(MyObj.class, 1, 2, 3.0));
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


    public static class MyObj {
        public MyObj(double a, Double b, byte c) {
        }
    }

    public static void main(String...strings) {
        final Factory f = new FactoryBuilder()
                .withWideningConversions()
                .build();
    }

}
