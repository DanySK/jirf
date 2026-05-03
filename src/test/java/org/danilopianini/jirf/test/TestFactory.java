package org.danilopianini.jirf.test;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.danilopianini.jirf.CreationResult;
import org.danilopianini.jirf.Factory;
import org.danilopianini.jirf.FactoryBuilder;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
class TestFactory {

    /**
     *
     */
    @Test
    void testWideningPrimitivesAndWrappers() {
        final Factory f = new FactoryBuilder().withWideningConversions().build();
        assertNotNull(f.build(MyObj.class, 1, 2, (byte) 3).getCreatedObjectOrThrowException());
    }

    /**
     *
     */
    @Test
    void testNonWidening() {
        final Factory f = new FactoryBuilder().withNarrowingConversions().build();
        assertNotNull(f.build(MyObj.class, 1, 2, 3.0).getCreatedObjectOrThrowException());
    }

    /**
     *
     */
    @Test
    void testEmptyVarargs() {
        final Factory f = new FactoryBuilder().withAutoBoxing().withWideningConversions().build();
        assertNotNull(f.build(MyObj.class, "").getCreatedObjectOrThrowException());
    }

    /**
     *
     */
    @Test
    void testSingleValuedVarargs() {
        final Factory f = new FactoryBuilder().withAutoBoxing().withWideningConversions().build();
        assertNotNull(f.build(MyObj.class, "", 1).getCreatedObjectOrThrowException());
    }

    /**
     *
     */
    @Test
    void testExpandedVarargs() {
        final Factory f = new FactoryBuilder().withAutoBoxing().withWideningConversions().build();
        assertNotNull(f.build(MyObj.class, "", 1, 2, 3, 4).getCreatedObjectOrThrowException());
    }

    /**
     *
     */
    @Test
    void testListEmbeddedVarargs() {
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
    void testSuperclasses() {
        final Factory f = new FactoryBuilder().withAutoBoxing().build();
        f.registerImplicit(CharSequence.class, double.class, s -> Double.parseDouble(s.toString()));
        assertNotNull(f.build(MyObj.class, "1", "2", (byte) 3).getCreatedObjectOrThrowException());
    }

    /**
     *
     */
    @Test
    void testSingleton() {
        final Factory f = new FactoryBuilder()
                .build();
        final Object o = new Object();
        f.registerSingleton(o);
        assertSame(o, f.build(Object.class).getCreatedObjectOrThrowException());
        final BigInteger s = new BigInteger("25");
        f.registerSingleton(Number.class, Object.class, s);
        assertSame(s, f.build(Number.class).getCreatedObjectOrThrowException());
        assertSame(s, f.build(Object.class).getCreatedObjectOrThrowException());
        final var bigInteger = f.build(BigInteger.class);
        assertTrue(bigInteger.getCreatedObject().isEmpty());
        assertNotNull(bigInteger.getExceptions());
        assertFalse(bigInteger.getExceptions().isEmpty());
        final var bigInteger2 = f.build(BigInteger.class, "ciao");
        assertTrue(bigInteger2.getCreatedObject().isEmpty());
        assertFalse(bigInteger2.getExceptions().isEmpty());
    }

    /**
     *
     */
    @Test
    void testVarArgsWithSingletons() {
        final Factory f = new FactoryBuilder()
            .withNarrowingConversions()
            .withArrayBooleanIntConversions()
            .withArrayListConversions(String[].class, Number[].class)
            .withArrayNarrowingConversions()
            .withAutomaticToString().build();
        final ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        f.registerSingleton(ZonedDateTime.class, now);
        f.registerSingleton(ZoneId.class, ZoneId.systemDefault());
        assertTrue(f.build(ReproduceGPSTrace.class, "gpsTrace", true, "AlignToSimulationTime").getCreatedObject().isPresent());
    }

    /**
     * Makes sure that no unexpected exceptions are thrown when a null parameter is passed and the object construction
     * correctly fails.
     */
    @Test
    void testErrorWhenNullIsAParameter() {
        final Factory factory = new FactoryBuilder().build();
        final CreationResult<MyObj> result = factory.build(MyObj.class, null, "foo");
        assertFalse(result.getExceptions().isEmpty());
    }
}
