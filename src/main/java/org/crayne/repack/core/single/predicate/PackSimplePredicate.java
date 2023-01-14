package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

// a single, simple value, e.g display.Name = "ipattern:*example*"
public class PackSimplePredicate implements PackPredicate {

    @NotNull
    protected final Token key;

    @NotNull
    protected final String value;

    @Nullable
    protected final PredicateType type;

    @Nullable
    private PackMatchPredicate parent;

    public PackSimplePredicate(@NotNull final Token key, @NotNull final String value, @Nullable final PredicateType type) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.parent = null;
    }

    @Nullable
    public PackMatchPredicate parent() {
        return parent;
    }

    public void parent(@Nullable final PackMatchPredicate parent) {
        this.parent = parent;
    }

    @NotNull
    public String toString() {
        return "PackSimplePredicate {" +
                "key = '" + key.token() + '\'' +
                ", value = '" + value + '\'' +
                ", type = " + type +
                '}';
    }

    @NotNull
    public Token key() {
        return key;
    }

    @NotNull
    public Set<Token> keys() {
        return Collections.singleton(key);
    }

    @NotNull
    public String value() {
        return value;
    }

    public @NotNull PredicateType type() {
        return type;
    }

    public boolean equals(@Nullable final Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final PackSimplePredicate that = (PackSimplePredicate) obj;
        return Objects.equals(this.key, that.key) &&
                Objects.equals(this.value, that.value) &&
                Objects.equals(this.type, that.type);
    }

    public int hashCode() {
        return Objects.hash(key, value, type);
    }

}
