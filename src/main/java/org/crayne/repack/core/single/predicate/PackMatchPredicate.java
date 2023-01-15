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

    private final int weight;

    public PackMatchPredicate(@NotNull final Collection<PackSimplePredicate> matchPredicates, final int weight) {
        this.predicates = new HashSet<>();
        this.matchPredicates = new ArrayList<>(matchPredicates);
        this.weight = weight;
    }

    public PackMatchPredicate(@NotNull final Collection<PackSimplePredicate> matchPredicates, @NotNull final Collection<PackPredicate> predicates, final int weight) {
        this.predicates = new HashSet<>(predicates);
        this.matchPredicates = new ArrayList<>(matchPredicates);
        this.weight = weight;
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

    public int weight() {
        return weight;
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
