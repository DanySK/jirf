package org.danilopianini.jirf;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Optional;

public final class ImmutableCreationResult<T> implements CreationResult<T> {

    private final Optional<T> result;
    private final ImmutableMap<Constructor<T>, InstancingImpossibleException> exceptions;

    private ImmutableCreationResult(
        @Nullable final T result,
        @Nonnull final ImmutableMap<Constructor<T>, InstancingImpossibleException> exceptions
    ) {
        this.result = Optional.ofNullable(result);
        this.exceptions = exceptions;
    }

    @Override
    public Optional<T> getCreatedObject() {
        return result;
    }

    @Override
    public T getCreatedObjectOrThrowException() {
        return result.orElseThrow(() -> {
            final var exception = new RuntimeException();
            for (final var entry: exceptions.values()) {
                exception.addSuppressed(entry);
            }
            throw exception;
        });
    }

    @Override
    public String toString() {
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
    public Map<Constructor<T>, InstancingImpossibleException> getExceptions() {
        return exceptions;
    }

    public static final class Builder<T> {

        private boolean isBuilt;
        private final ImmutableMap.Builder<Constructor<T>, InstancingImpossibleException> mapBuilder = new ImmutableMap.Builder<>();
        private T result;

        public Builder<T> withFailure(
            @Nonnull final Constructor<T> constructor,
            @Nonnull final InstancingImpossibleException error
        ) {
            consistencyCheck();
            mapBuilder.put(constructor, error);
            return this;
        }

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

        public ImmutableCreationResult<T> build() {
            isBuilt = true;
            return new ImmutableCreationResult<T>(this.result, this.mapBuilder.build());
        }
    }
}
