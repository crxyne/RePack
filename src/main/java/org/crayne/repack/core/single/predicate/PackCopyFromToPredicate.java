package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class PackCopyFromToPredicate implements PackPredicate {

    @NotNull
    protected final String key;

    @NotNull
    protected final String value;

    @Nullable
    private PackCopyPredicate parent;

    public PackCopyFromToPredicate(@NotNull final String key, @NotNull final String value) {
        this.key = key;
        this.value = value;
        this.parent = null;
    }

    @Nullable
    public PackCopyPredicate parent() {
        return parent;
    }

    public void parent(@Nullable final PackCopyPredicate parent) {
        this.parent = parent;
    }

    @NotNull
    public String toString() {
        return "PackSimplePredicate {" +
                "key = '" + key + '\'' +
                ", value = '" + value + '\'' +
                '}';
    }

    @NotNull
    public String key() {
        return key;
    }

    @NotNull
    public Set<Token> keys() {
        return Collections.singleton(Token.of(key));
    }

    @NotNull
    public String value() {
        return value;
    }

    @NotNull
    public PredicateType type() {
        return PredicateType.COPY_FROM_TO;
    }

    public boolean equals(@Nullable final Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final PackCopyFromToPredicate that = (PackCopyFromToPredicate) obj;
        return Objects.equals(this.key, that.key) &&
                Objects.equals(this.value, that.value);
    }

    public int hashCode() {
        return Objects.hash(key, value);
    }

}
