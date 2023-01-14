package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface PackPredicate {

    @NotNull
    String value();

    @NotNull
    Collection<Token> keys();

    @NotNull
    PredicateType type();

}
