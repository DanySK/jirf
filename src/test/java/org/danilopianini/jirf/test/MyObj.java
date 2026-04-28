package org.danilopianini.jirf.test;

// CHECKSTYLE: EmptyStatement OFF
// CHECKSTYLE: NeedBraces OFF
// CHECKSTYLE: JavadocType OFF
// CHECKSTYLE: JavadocMethod OFF
// CHECKSTYLE: RedundantModifier OFF
public final class MyObj {

    public MyObj(final double a, final Double b, final byte c) { } // NOPMD

    public MyObj(final String a, final double... b) {
        for (int i = 0; i < b.length; i++); // NOPMD
    }
}
