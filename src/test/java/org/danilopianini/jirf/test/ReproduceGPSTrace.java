package org.danilopianini.jirf.test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// CHECKSTYLE: JavadocType OFF
// CHECKSTYLE: JavadocMethod OFF
// CHECKSTYLE: RedundantModifier OFF
public final class ReproduceGPSTrace {

    public ReproduceGPSTrace(
        final ZonedDateTime calendar,
        final ZoneId timezone,
        final String path,
        final boolean cycle,
        final String normalizer,
        final Object... normalizerArgs
    ) {
        this(calendar, timezone, 0, path, cycle, normalizer, normalizerArgs);
    }

    public ReproduceGPSTrace(
        final ZonedDateTime calendar,
        final ZoneId timezone,
        final double speed,
        final String path,
        final boolean cycle,
        final String normalizer,
        final Object... normalizerArgs
    ) {
        Objects.requireNonNull(calendar);
        Objects.requireNonNull(timezone);
        assertEquals(0, speed, 0d);
        Objects.requireNonNull(path);
        assertTrue(cycle);
        Objects.requireNonNull(normalizer);
        Objects.requireNonNull(normalizerArgs);
    }
}
