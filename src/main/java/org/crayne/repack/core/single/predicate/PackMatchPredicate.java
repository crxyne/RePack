package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.parsing.lexer.Token;
import org.crayne.repack.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PackMatchPredicate implements PackPredicate {

    @NotNull
    private final Set<PackPredicate> predicates;

    @NotNull
    private final List<PackSimplePredicate> matchPredicates;

    public PackMatchPredicate(@NotNull final Collection<PackSimplePredicate> matchPredicates) {
        this.predicates = new HashSet<>();
        this.matchPredicates = new ArrayList<>(matchPredicates);
    }

    public PackMatchPredicate(@NotNull final Collection<PackSimplePredicate> matchPredicates, @NotNull final Collection<PackPredicate> predicates) {
        this.predicates = new HashSet<>(predicates);
        this.matchPredicates = new ArrayList<>(matchPredicates);
    }

    @NotNull
    public Set<PackPredicate> predicates() {
        return predicates;
    }

    @NotNull
    public List<PackSimplePredicate> matchPredicates() {
        return matchPredicates;
    }

    @NotNull
    public String value() {
        throw new RuntimeException("Match predicates cannot have one single value");
    }

    @NotNull
    public PredicateType type() {
        return PredicateType.MATCH;
    }

    @NotNull
    public List<Token> keys() {
        return predicates.stream().map(PackPredicate::keys).flatMap(Collection::stream).toList();
    }

    @NotNull
    public String toString() {
        return "PackMatchPredicate {\n" +
                ("match = " + StringUtil.stringOf(matchPredicates) +
                ", predicates = " + StringUtil.stringOf(predicates)).indent(3) +
                "}";
    }

}
