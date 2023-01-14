package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.parsing.lexer.Token;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

// the kind of SETALL_PREDICATE, where you can set all certain types of items for example to a single texture
public record PackSupredicate(@NotNull PredicateType type, @NotNull String value) implements PackPredicate {

    @NotNull
    public String toString() {
        return "PackSupredicate {" +
                "type = " + type +
                ", value = '" + value + '\'' +
                '}';
    }


    @NotNull
    public Set<Token> keys() {
        return Collections.emptySet(); // more handling required; putting every single MinecratItem enum member here is completely pointless
    }

}
