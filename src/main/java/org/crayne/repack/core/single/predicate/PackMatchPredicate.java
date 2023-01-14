package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.parsing.lexer.Token;
import org.crayne.repack.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PackMatchPredicate extends PackSimplePredicate {

    @NotNull
    private final Set<PackPredicate> predicates;

    public PackMatchPredicate(@NotNull final Token key, @NotNull final String predicate) {
        super(key, predicate, PredicateType.MATCH);
        this.predicates = new HashSet<>();
    }

    public PackMatchPredicate(@NotNull final Token key, @NotNull final String predicate, @NotNull final Collection<PackPredicate> predicates) {
        super(key, predicate, PredicateType.MATCH);
        this.predicates = new HashSet<>(predicates);
    }

    @NotNull
    public Set<PackPredicate> predicates() {
        return predicates;
    }

    @NotNull
    public String predicate() {
        return super.predicate();
    }

    @NotNull
    public PredicateType type() {
        return PredicateType.MATCH;
    }

    @NotNull
    public String toString() {
        return "PackMatchPredicate {\n" +
                ("key = '" + key.token() + '\'' +
                ", predicate = '" + predicate + '\'' +
                ", type = " + type +
                ", \n" +
                "predicates = " + StringUtil.stringOf(predicates)).indent(3) +
                "}";
    }

}
