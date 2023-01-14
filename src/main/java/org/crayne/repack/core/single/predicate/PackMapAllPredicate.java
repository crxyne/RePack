package org.crayne.repack.core.single.predicate;

import org.crayne.repack.core.single.PredicateType;
import org.crayne.repack.parsing.lexer.Token;
import org.crayne.repack.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// a list of items/armor/elytra types mapped to a single certain texture file
public class PackMapAllPredicate implements PackPredicate {

    @NotNull
    private final PredicateType type;

    @NotNull
    private final List<Token> keys;

    @NotNull
    private final String value;

    public PackMapAllPredicate(@NotNull final PredicateType type, @NotNull final Collection<Token> keys, @NotNull final String value) {
        this.type = type;
        this.keys = new ArrayList<>(keys);
        this.value = value;
    }

    @NotNull
    public String value() {
        return value;
    }

    @NotNull
    public List<Token> keys() {
        return keys;
    }

    @NotNull
    public PredicateType type() {
        return type;
    }

    @NotNull
    public String toString() {
        return "PackMapAllPredicate {" +
                "type = " + type +
                ", keys = " + StringUtil.stringOf(keys.stream().map(Token::token).toList()) +
                ", value = '" + value + '\'' +
                '}';
    }
}
