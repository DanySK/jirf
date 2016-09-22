/**
 * 
 */
package org.danilopianini.jirf;

import java.util.function.Function;

import org.jgrapht.EdgeFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 *
 */
public class ImplicitEdgeFactory implements EdgeFactory<Class<?>, Function<?, ?>> {

    private final Table<Class<?>, Class<?>, Function<?, ?>> implicitConversions = HashBasedTable.create();

    /**
     * @param source
     * @param dest
     * @param implicit
     * @param S
     * @param D
     */
    public <S, D> void addImplicitConversion(final Class<S> source, final Class<D> dest, final Function<? super S, ? extends D> implicit) {
        implicitConversions.put(source, dest, implicit);
    }

    @Override
    public Function<?, ?> createEdge(final Class<?> sourceVertex, final Class<?> targetVertex) {
        final Function<?, ?> result = implicitConversions.get(sourceVertex, targetVertex);
        if (result == null) {
            throw new IllegalStateException("No conversion function was provided for " + sourceVertex.getSimpleName() + " -> " + targetVertex.getSimpleName());
        }
        return result;
    }

}
