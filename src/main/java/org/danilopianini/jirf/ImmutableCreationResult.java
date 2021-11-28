package org.danilopianini.jirf;

import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Optional;

/**
 * Immutable implementation of {@link CreationResult}
 * (of course, if T is mutable, the object state can be indirectly mutated).
 *
 * @param <T> the created object type
 */
public final class ImmutableCreationResult<T> implements CreationResult<T>, Serializable {

    private final T result;
    private final ImmutableMap<Constructor<T>, InstancingImpossibleException> exceptions;

    private ImmutableCreationResult(
        @Nullable final T result,
        @Nonnull final ImmutableMap<Constructor<T>, InstancingImpossibleException> exceptions
    ) {
        this.result = result;
        this.exceptions = exceptions;
    }

    @Override
    public Optional<T> getCreatedObject() {
        return Optional.ofNullable(result);
    }

    @Override
    public T getCreatedObjectOrThrowException() {
        return getCreatedObject().orElseThrow(() -> {
            final var exception = new RuntimeException();
            for (final var entry: exceptions.values()) {
                exception.addSuppressed(entry);
            }
            throw exception;
        });
    }

    @Override
    public String toString() {
        final var result = getCreatedObject();
        return "Result{" + (result.isPresent() ? result.get() : exceptions) + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ImmutableCreationResult<?> that = (ImmutableCreationResult<?>) o;
        return new EqualsBuilder().append(result, that.result).append(exceptions, that.exceptions).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(result).append(exceptions).toHashCode();
    }

    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "the field is immutable")
    public ImmutableMap<Constructor<T>, InstancingImpossibleException> getExceptions() {
        return exceptions;
    }

    /**
     * Builder for an {@link ImmutableCreationResult}.
     *
     * @param <T> the created object type
     */
    public static final class Builder<T> {

        private boolean isBuilt;
        private final ImmutableMap.Builder<Constructor<T>, InstancingImpossibleException> mapBuilder =
            new ImmutableMap.Builder<>();
        private T result;

        /**
         * @param constructor the constructor that failed
         * @param error the exception that the failure produced
         * @return this buider for method chaining
         */
        public Builder<T> withFailure(
            @Nonnull final Constructor<T> constructor,
            @Nonnull final InstancingImpossibleException error
        ) {
            consistencyCheck();
            mapBuilder.put(constructor, error);
            return this;
        }

        /**
         * @param result the created object
         * @return this buider for method chaining
         */
        public Builder<T> withResult(@Nonnull final T result) {
            if (this.result != null) {
                throw new IllegalStateException("The result of this build has already been set.");
            }
            this.result = result;
            return this;
        }

        private void consistencyCheck() {
            if (isBuilt) {
                throw new IllegalStateException(this.getClass().getSimpleName() + " cannot be modified after build"
                    + "(but can be used multiple times with the same configuration)");
            }
        }

        /**
         * @return the {@link CreationResult} built with the provided information
         */
        public ImmutableCreationResult<T> build() {
            isBuilt = true;
            return new ImmutableCreationResult<>(this.result, this.mapBuilder.build());
        }
    }
}
