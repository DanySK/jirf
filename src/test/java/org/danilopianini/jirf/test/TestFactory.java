package org.danilopianini.jirf.test;

import org.danilopianini.jirf.Factory;
import org.danilopianini.jirf.FactoryBuilder;
import org.junit.Assert;
import org.junit.Test;

public final class TestFactory {

    @Test
    public void testWideningPrimitivesAndWrappers() {
        final Factory f = new FactoryBuilder()
//                .withAutoBoxing()
                .withWideningConversions()
                .build();
        Assert.assertNotNull(f.build(MyObj.class, 1, 2, (byte) 3));
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
