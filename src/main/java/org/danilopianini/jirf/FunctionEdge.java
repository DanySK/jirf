package org.danilopianini.jirf;

import java.util.Objects;
import java.util.function.Function;

import org.jgrapht.graph.DefaultEdge;

final class FunctionEdge {

    private final Class<?> source;
    private final Class<?> destination;
    private final Function<?, ?> function;
    private int hash;

    public FunctionEdge(final Class<?> source, final Class<?> destination, final Function<?, ?> function) {
        this.source = Objects.requireNonNull(source);
        this.destination = Objects.requireNonNull(destination);
        this.function = Objects.requireNonNull(function);
    }

    public Class<?> getSource() {
        return source;
    }

    public Class<?> getDestination() {
        return destination;
    }

    public Function<?, ?> getFunction() {
        return function;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FunctionEdge) {
            final FunctionEdge fe = (FunctionEdge) obj;
            return source.equals(fe.source)
                    && destination.equals(fe.destination);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = source.hashCode() ^ destination.hashCode();
        }
        return hash;
    }
    
    @Override
    public String toString() {
        return source + " => " + destination;
    }

}
