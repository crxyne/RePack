package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PackCopyPredicate implements PackPredicate {

    @NotNull
    private final Map<String, String> copyFiles;

    public PackCopyPredicate(@NotNull final Collection<PackCopyFromToPredicate> copyFromToPredicates) {
        copyFiles = new HashMap<>(copyFromToPredicates.stream()
                .map(p -> Map.entry(p.key(), p.value()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @NotNull
    public Map<String, String> copyFiles() {
        return copyFiles;
    }

    @NotNull
    public String value() {
        throw new RuntimeException("Pack copy predicates do not have a value");
    }

    @NotNull
    public Collection<Token> keys() {
        throw new RuntimeException("Pack copy predicates do not have token keys");
    }

    @NotNull
    public PredicateType type() {
        return PredicateType.COPY;
    }

}
