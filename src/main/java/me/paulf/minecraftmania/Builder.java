package me.paulf.minecraftmania;

import java.util.Objects;
import java.util.function.Function;

public class Builder<T> {
    final T object;
    String name;
    final HintFactory<T> hint;

    public Builder(final T object, final String name, final HintFactory<T> hint) {
        this.object = object;
        this.name = name;
        this.hint = hint;
    }

    public Builder<T> map(final Function<String, String> mapper) {
        this.name = mapper.apply(this.name);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        final Builder<?> builder = (Builder<?>) o;
        return Objects.equals(this.name, builder.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    public HintedWord<T> build() {
        return new HintedWord<>(this.object, this.name, this.hint);
    }
}
